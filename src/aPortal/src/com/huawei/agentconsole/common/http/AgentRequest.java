
package com.huawei.agentconsole.common.http;

import javax.net.ssl.SSLContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.FileUtils;
import com.huawei.agentconsole.common.util.JsonUtils;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;

public class AgentRequest
{
    /**
     * log
     */
    private static final Logger LOG = LoggerFactory.getLogger(AgentRequest.class);
    
    /**
     * Max connections of connection pool,unit:millisecond
     */
    private static final int MAXCONNECTION = 500;
    
    /**
     * Connections of every route,unit:millisecond
     */
    private static final int MAXPERROUTE = 500;
    
    /**
     * Max request time of getting a connection from connection pool,unit:millisecond
     */
    private static final int REQUESTTIMEOUT = 5000;
    
    /**
     * Max time of a request,unit:millisecond
     */
    private static final int CONNECTIMEOUT = 30000;
    
    /**
     * Max time of waiting for response message,unit:millisecond
     */
    private static final int SOCKETIMEOUT = 60000;

    
    private static PoolingHttpClientConnectionManager connManager = null;
    
    private static CloseableHttpClient client = null;
    
    public static void init()
    {
        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                
                @Override
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
        
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext
                    , new X509HostnameVerifier(){
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
                public void verify(String host, SSLSocket ssl)
                        throws IOException {
                }
                public void verify(String host, X509Certificate cert)
                        throws SSLException {
                }
                public void verify(String host, String[] cns,
                        String[] subjectAlts) throws SSLException {
                }
            });
            
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();
            
            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            connManager.setMaxTotal(MAXCONNECTION);
            connManager.setDefaultMaxPerRoute(MAXPERROUTE);
            
        } 
        catch (RuntimeException e) 
        {
            throw e;
        }
        catch (Exception e)
        {
            LOG.error("init connection pool failed \r\n {}: ", LogUtils.encodeForLog(e.getMessage()));
            return;
        }
        
        client = getConnection();
    }
    

    private static CloseableHttpClient getConnection()
    {
        RequestConfig restConfig = RequestConfig.custom().setConnectionRequestTimeout(REQUESTTIMEOUT)
                .setConnectTimeout(CONNECTIMEOUT)
                .setSocketTimeout(SOCKETIMEOUT).build();
        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler()
        {
            public boolean retryRequest(IOException exception, int executionCount,
                    HttpContext context)
            {
                if (executionCount >= 3)
                {
                   return false; 
                }
                if (exception instanceof NoHttpResponseException) 
                {
                    return true;  
                } 
                if (exception instanceof InterruptedIOException) 
                {
                    return false;
                }
                if (exception instanceof SSLHandshakeException) 
                {
                    return false;  
                }  
                if (exception instanceof UnknownHostException) 
                {
                    return false;  
                }  
                if (exception instanceof ConnectTimeoutException) 
                {
                    return false;  
                }  
                if (exception instanceof SSLException) 
                {
                    return false;  
                }
                
                HttpClientContext clientContext = HttpClientContext.adapt(context);  
                HttpRequest request = clientContext.getRequest();  
                if (!(request instanceof HttpEntityEnclosingRequest)) 
                {  
                    return true;  
                }  
                return false;  
            }
        };
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableCookieManagement()
                .setConnectionManager(connManager).setDefaultRequestConfig(restConfig).setRetryHandler(retryHandler).build();
        return httpClient;
    }
    
    
    
    /**
     * Send http's GET request
     * @param agentId: : the agent id
     * @param url:the address of the request
     * @return
     */
    public static Map<String, Object> get(String agentId, String url)
    {
        CloseableHttpResponse response = null;
        HttpGet get = null;
        Map<String, Object> result  = null;
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            get = new HttpGet(url);
            
            setHeaders(agentId, get);
            
            get.setHeader("Content-Type", "application/json;charset=UTF-8");
            response = client.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                if (null != entity)
                {
                    String entityContent = EntityUtils.toString(entity,"UTF-8");
                    result = JsonUtils.jsonToMap(entityContent);          
                }
                else
                {
                    result = returnContentError();
                }
                try 
                {
                    EntityUtils.consume(entity);
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            else
            {
            	result = returnParamError(agentId, statusCode);
            }
        }
        catch (UnsupportedEncodingException e) 
        {
            result = returnConnectError(agentId, e);
        } catch (ClientProtocolException e)
        {
            result = returnConnectError(agentId, e);
        }
        catch (IOException e) 
        {
            result =  returnConnectError(agentId, e);
        }
        finally
        {
            if (null != response)
            {
                try
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release response failed \r\n {}",
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        
        return result;
    }
    
    /**
     * 通过调用阿根廷接口获取文件流，之后将文件流临时转换存储在本地临时目录，保存成文件
     * 之后将文件再次转成输出流 ，返回给调用者，之所以这么做是因为agw接口那边暂时没有带content-length所以需要本地计算，在响应中带上去，这样可以网页在线在线试听
     * @param filename
     * @param agentId
     * @param url
     * @param response
     */
    public static void fileGet(String filename, String agentId, String url, HttpServletResponse response)
    {
        CloseableHttpResponse httpResponse = null;
        HttpGet get = null;
        //用于处理agw响应的文件流
        InputStream in = null;
        OutputStream out = null;
        
        //用于处理转化本地文件的文件流
        FileInputStream inputStream = null;
        OutputStream outputStream = null;
        
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            get = new HttpGet(url);
            
            setHeaders(agentId, get);
            
            response.setContentType("audio/x-wav");
            response.addHeader("Cache", "no-cache");
            response.addHeader("Accept-Ranges", "bytes");
            response.addHeader("Cache-Control", "no-store,no-cache");

            response.addHeader("Content-Disposition", "attachment;filename="+filename );
           
            httpResponse = client.execute(get);           
            
            long length = 0;
            
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = httpResponse.getEntity();
                
                StringBuffer bufferString = new StringBuffer();
                
                bufferString.append(GlobalObject.getApp_path());
                bufferString.append("/tempfile/");
                bufferString.append(agentId+"/");
                bufferString.append(filename);
                
                File tempFile = FileUtils.createRecordFile(bufferString.toString());
                if (null == tempFile)
                {
                    LOG.error(LogUtils.AGENT_ID + "Create tempfile failed",
                            LogUtils.encodeForLog(agentId));
                    return;
                }
                
                if (null != entity)
                {
                    try
                    {
                        in = entity.getContent();                        
                        byte[] buffer = new byte[1024];
                        out = new FileOutputStream(tempFile);

                        //循环读取所有字节流
                        int i = 0;                        
                        while ((i = in.read(buffer)) > 0)
                        {
                            out.write(buffer,0,i);
                            length = length + i;
                        }

                        out.flush();                        
                        inputStream = new FileInputStream(tempFile);
                        
                        //此处加入header到响应中
                        response.addHeader("Content-Length", length+"");
                        response.addHeader("Content-Range", "bytes 0-" + (length-1) + "/" + length);
                        outputStream = response.getOutputStream();
                        
                        int j = 0;
                        byte[] fileBuffer = new byte[1024];
                        while ((j = inputStream.read(fileBuffer)) > 0)
                        {
                            outputStream.write(fileBuffer,0,j);
                        }
                        outputStream.flush();
                        
                    } 
                    catch (IOException e)
                    {
                        LOG.error(LogUtils.AGENT_ID + "output failed \r\n {}",
                                LogUtils.encodeForLog(agentId), 
                                LogUtils.encodeForLog(e.getMessage()));
                    }
                    finally
                    {                         
                        try
                        {
                            if(in != null)
                            {
                                in.close();
                            }
                        }
                        catch (IOException e) 
                        {
                            LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                                    LogUtils.encodeForLog(agentId), 
                                    LogUtils.encodeForLog(e.getMessage()));
                        }    
                        
                        try
                        {
                            if(null != out)
                            {
                                out.close();
                            }
                          
                        }
                        catch (IOException e) 
                        {
                            LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                                    LogUtils.encodeForLog(agentId), 
                                    LogUtils.encodeForLog(e.getMessage()));
                        }
                        
                        try
                        {
                            if(null != inputStream)
                            {
                                inputStream.close();
                            }
                        }
                        catch (IOException e) 
                        {
                            LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                                    LogUtils.encodeForLog(agentId), 
                                    LogUtils.encodeForLog(e.getMessage()));
                        } 
                        
                        try
                        {
                            if(null != outputStream)
                            {
                                outputStream.close();
                            }
                        }
                        catch (IOException e) 
                        {
                            LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                                    LogUtils.encodeForLog(agentId), 
                                    LogUtils.encodeForLog(e.getMessage()));
                        } 
                        FileUtils.deleteFile(tempFile);  
                        
                        EntityUtils.consume(entity);
                    }
                }   

            }
            else
            {
                LOG.error(LogUtils.formatMap(returnContentError()));
            }
        }
        catch (UnsupportedEncodingException e) 
        {
            LOG.error(LogUtils.formatMap(returnContentError()));
        } catch (ClientProtocolException e)
        {
            LOG.error(LogUtils.formatMap(returnContentError()));
        }
        catch (IOException e) 
        {
            LOG.error(LogUtils.formatMap(returnContentError()));
        }
        finally
        {
            if (null != httpResponse)
            {
                try
                {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release httpResponse failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        
    }
    
    
    /**
     * 生成录音临时文件
     * @param filename 
     * @param agentId
     * @param url
     * @return  生成的临时文件的路径
     */
    public static String genRecordTempFile(String fileName, String agentId, String url)
    {
        CloseableHttpResponse httpResponse = null;
        HttpGet get = null;
        //用于处理agw响应的文件流
        InputStream in = null;
        OutputStream out = null;
        
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            get = new HttpGet(url);
            
            setHeaders(agentId, get);
            
            httpResponse = client.execute(get);           
            
            long length = 0;
            
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = httpResponse.getEntity();
                
                StringBuffer bufferString = new StringBuffer();
                
                bufferString.append(GlobalObject.getApp_path());
                bufferString.append(CommonConstant.TEMPFILE_RECORD_PATH);
                bufferString.append("/");
                bufferString.append(agentId);
                bufferString.append("/");
                bufferString.append(fileName);
                
                String tempFilePath = bufferString.toString(); //临时文件路径
                File tempFile = FileUtils.createRecordFile(tempFilePath);
                if (null == tempFile)
                {
                    LOG.error(LogUtils.AGENT_ID + "Create tempfile failed",
                            LogUtils.encodeForLog(agentId));
                    return null;
                }
                
                if (null != entity)
                {
                    try
                    {
                        in = entity.getContent();                        
                        byte[] buffer = new byte[1024];
                        out = new FileOutputStream(tempFile);
                        
                        //循环读取所有字节流
                        int i = 0;                        
                        while ((i = in.read(buffer)) > 0)
                        {
                            out.write(buffer,0,i);
                            length = length + i;
                        }                        
                        out.flush();    
                        return tempFilePath;
                    } 
                    catch (IOException e)
                    {
                        LOG.error(LogUtils.AGENT_ID + "output failed \r\n {}",
                                LogUtils.encodeForLog(agentId), 
                                LogUtils.encodeForLog(e.getMessage()));
                    }
                    finally
                    {                         
                        try
                        {
                            if(in != null)
                            {
                                in.close();
                            }
                        }
                        catch (IOException e) 
                        {
                            LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                                    LogUtils.encodeForLog(agentId), 
                                    LogUtils.encodeForLog(e.getMessage()));
                        }    
                        
                        try
                        {
                            if(null != out)
                            {
                                out.close();
                            }
                            
                        }
                        catch (IOException e) 
                        {
                            LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                                    LogUtils.encodeForLog(agentId), 
                                    LogUtils.encodeForLog(e.getMessage()));
                        }
                        
                        EntityUtils.consume(entity);
                    }
                }   
                
            }
            else
            {
                LOG.error(LogUtils.formatMap(returnContentError()));
            }
        }
        catch (UnsupportedEncodingException e) 
        {
            LOG.error(LogUtils.formatMap(returnContentError()));
        } catch (ClientProtocolException e)
        {
            LOG.error(LogUtils.formatMap(returnContentError()));
        }
        catch (IOException e) 
        {
            LOG.error(LogUtils.formatMap(returnContentError()));
        }
        finally
        {
            if (null != httpResponse)
            {
                try
                {
                    EntityUtils.consume(httpResponse.getEntity());
                    httpResponse.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release httpResponse failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return null;        
    }

    
    /**
     * Send http's POST request
     * @param agentId: the agent id
     * @param url:the address of the request
     * @param entityParams:the paramters of entity
     * @return
     */
    public static Map<String, Object> post(String agentId, String url, Object entityParams)
    {
        Map<String, Object> result = null;
        HttpPost post = null;
        CloseableHttpResponse response = null;
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            post = new HttpPost(url);
            if (null != entityParams)
            {               
                String jsonString = JsonUtils.beanToJson(entityParams);
                HttpEntity entity = new StringEntity(jsonString);
                post.setEntity(entity);
            }
            setHeaders(agentId, post);
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                if (null != entity)
                {
                    String entityContent = EntityUtils.toString(entity,"UTF-8");
                    result = JsonUtils.jsonToMap(entityContent);
                }
                else
                {
                    result = returnContentError();
                }
                try 
                {
                    EntityUtils.consume(entity);
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}",
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            else
            {
            	result = returnParamError(agentId, statusCode);
            }
        }
        catch (UnsupportedEncodingException e) 
        {
            result = returnConnectError(agentId, e);
        } catch (ClientProtocolException e)
        {
            result = returnConnectError(agentId, e);
        }
        catch (IOException e) 
        {
            result =  returnConnectError(agentId, e);
        }
        finally
        {
            if (null != response)
            {
                try
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release response failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return result;
    }
    
    /**
     * Send http's PUT request
     * @param agentId: the agent id
     * @param url:the address of the request
     * @param entityParams:the paramters of entity
     * @param isLogin:是否为登录接口
     * @return
     */
    public static Map<String, Object> loginPut(String agentId, String url, Object entityParams, AgentBaseInfoBean agentAuthInfoBean)
    {
        CloseableHttpResponse response = null;
        HttpPut put = null;
        Map<String, Object> result = null;
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            put = new HttpPut(url);
            if (null != entityParams) {
                String jsonString = JsonUtils.beanToJson(entityParams);
                HttpEntity entity = new StringEntity(jsonString);
                put.setEntity(entity);
            }
                    
            put.setHeader("Content-Type", "application/json;charset=UTF-8");
            response = client.execute(put);         
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                if (null != entity)
                {
                    String entityContent = EntityUtils.toString(entity,"UTF-8");
                    result = JsonUtils.jsonToMap(entityContent);
                    if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
                    {
                        //只有登录成功，才获取guid和cookie
                        getGuidAndCookie(agentId, response, agentAuthInfoBean);
                    }
                }
                else
                {
                    result = returnContentError();
                }
                
                
                try 
                {
                    EntityUtils.consume(entity);
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n{} ", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            else
            {
            	result = returnParamError(agentId, statusCode);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            result = returnConnectError(agentId, e);
        } 
        catch (ClientProtocolException e)
        {
            result =  returnConnectError(agentId, e);
        } 
        catch (IOException e)
        {
            result = returnConnectError(agentId, e);
        }
        finally
        {
            if (null != response)
            {
                try
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release response failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return result;
    }    
    
    public static Map<String, Object> loginPutByAccount(String account, String url, Object entityParams, AgentBaseInfoBean agentAuthInfoBean)
    {
        CloseableHttpResponse response = null;
        HttpPut put = null;
        Map<String, Object> result = null;
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            put = new HttpPut(url);
            if (null != entityParams) {
                String jsonString = JsonUtils.beanToJson(entityParams);
                HttpEntity entity = new StringEntity(jsonString);
                put.setEntity(entity);
            }
                    
            put.setHeader("Content-Type", "application/json;charset=UTF-8");
            response = client.execute(put);         
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                if (null != entity)
                {
                    String entityContent = EntityUtils.toString(entity,"UTF-8");
                    result = JsonUtils.jsonToMap(entityContent);
                    if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
                    {
                      //只有登录成功，才获取guid和cookie
                        @SuppressWarnings("unchecked")
                        Map<String, Object> content = (Map<String, Object>) result.get("result");
                        if (null != content)
                        {
                            String workno = String.valueOf(content.get("workno"));
                            agentAuthInfoBean.setAgentId(workno);
                            getGuidAndCookie(workno, response, agentAuthInfoBean);
                        }
                    }
                }
                else
                {
                    result = returnContentError();
                }
                
                
                try 
                {
                    EntityUtils.consume(entity);
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.ACCOUNT + "release entity failed \r\n{} ",
                            LogUtils.encodeForLog(account), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            else
            {
                result = returnParamError(account, statusCode);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            result = returnConnectError(account, e);
        } 
        catch (ClientProtocolException e)
        {
            result =  returnConnectError(account, e);
        } 
        catch (IOException e)
        {
            result = returnConnectError(account, e);
        }
        finally
        {
            if (null != response)
            {
                try
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.ACCOUNT + "release response failed \r\n {}", 
                            LogUtils.encodeForLog(account), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return result;
    }   
    /**
     * Send http's PUT request
     * @param agentId: the agent id
     * @param url:the address of the request
     * @param entityParams:the paramters of entity
     * @param isLogin:是否为登录接口
     * @return
     */
    public static Map<String, Object> put(String agentId, String url, Object entityParams)
    {
        CloseableHttpResponse response = null;
        HttpPut put = null;
        Map<String, Object> result = null;
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            put = new HttpPut(url);
            if (null != entityParams) {
                String jsonString = JsonUtils.beanToJson(entityParams);
                HttpEntity entity = new StringEntity(jsonString);
                put.setEntity(entity);
            }
            
            setHeaders(agentId, put);
            
                        
            put.setHeader("Content-Type", "application/json;charset=UTF-8");
            response = client.execute(put);         
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                if (null != entity)
                {
                    String entityContent = EntityUtils.toString(entity,"UTF-8");
                    result = JsonUtils.jsonToMap(entityContent);
                }
                else
                {
                    result = returnContentError();
                }
                try 
                {
                    EntityUtils.consume(entity);
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n{} ",
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            else
            {
            	result = returnParamError(agentId, statusCode);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            result = returnConnectError(agentId, e);
        } 
        catch (ClientProtocolException e)
        {
            result =  returnConnectError(agentId, e);
        } 
        catch (IOException e)
        {
            result = returnConnectError(agentId, e);
        }
        finally
        {
            if (null != response)
            {
                try
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release response failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return result;
    }    
    
    
    /**
     * Send http's Delete request
     * @param url:the address of the request
     * @param headers:the field is used to set the header of http request
     * @return
     */
    public static Map<String, Object> delete(String agentId, String url)
    {

        CloseableHttpResponse response = null;
        MyHttpDelete delete = null;
        Map<String, Object> result = null;
        try 
        {
            url = Normalizer.normalize(url, Form.NFKC);
            delete = new MyHttpDelete(url);
            setHeaders(agentId, delete);   
            delete.setHeader("Content-Type", "application/json;charset=UTF-8");
            response = client.execute(delete);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                if (null != entity)
                {
                    String entityContent = EntityUtils.toString(entity,"UTF-8");
                    result = JsonUtils.jsonToMap(entityContent);                    
                }
                else
                {
                    result = returnContentError();
                }
                
                try 
                {
                    EntityUtils.consume(entity);
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release entity failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            else
            {
            	result = returnParamError(agentId, statusCode);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            result = returnConnectError(agentId, e);
        } 
        catch (ClientProtocolException e) 
        {
           result =  returnConnectError(agentId, e);
        }
        catch (IOException e) 
        {
            result = returnConnectError(agentId, e);
        }
        finally
        {
            if (null != response)
            {
                try
                {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } 
                catch (IOException e) 
                {
                    LOG.error(LogUtils.AGENT_ID + "release response failed \r\n {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
                }
            }
            
        }
        return result;
        
    }
    
    
    /**
     * 获取登录成功后的Guid
     * @param agentId
     * @param response
     * @param agentBaseInfoBean
     * 
     */
    private static void getGuidAndCookie(String agentId, CloseableHttpResponse response, AgentBaseInfoBean agentBaseInfoBean)
    {
        Header[] allHeaders = response.getAllHeaders();
        if (allHeaders == null || allHeaders.length == 0)
        {
            return;
        }
        
        StringBuffer cookieBuffer = new StringBuffer();
        for (Header header : allHeaders)
        {
            if (header.getName().equals("Set-GUID"))
            {
                String setGuid = header.getValue();
                if (setGuid != null)
                {
                    String guid = setGuid.replace("JSESSIONID=", "");
                    agentBaseInfoBean.setGuid(guid);
                }
            }  
            else if (header.getName().equals("Set-Cookie"))
            {
                String setCookie = header.getValue();
                if (setCookie != null)
                {
                    cookieBuffer.append(setCookie).append(";");
                    agentBaseInfoBean.setCookie(cookieBuffer.toString());
                }
            }
        }
        
        GlobalObject.addAgentBaseInfo(agentId, agentBaseInfoBean);
    }
  

    
    /**
     * header set mothed abstract 
     * 
     * @param agentId
     * @param httpMethod
     */
    private static void setHeaders(String agentId, HttpRequestBase httpMethod)
    {
        AgentBaseInfoBean agentBaseInfo = GlobalObject.getAgentBaseInfo(agentId);
        if (null != agentBaseInfo)
        {
            httpMethod.setHeader("guid", formatHeader(agentBaseInfo.getGuid()));
            httpMethod.setHeader("Cookie", formatCookie(agentBaseInfo.getCookie()));
        }
        
    }
    
    /**
     * 返回ConnectotError信息 
     * @param e
     * @return
     */
    private static Map<String, Object> returnParamError(String agentId, int statusCode)
    {
        LOG.error(LogUtils.AGENT_ID + "request to server, but not return 200 OK, the return is {}", 
                LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(statusCode));
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("retcode", AgentErrorCode.AGENT_REST_INVALID);
        resultMap.put("message", "The status code is :" + statusCode);
        return resultMap;
    }
    
    /**
     * 返回ConnectotError信息 
     * @param e
     * @return
     */
    private static Map<String, Object> returnConnectError(String agentId, Exception e)
    {
        LOG.error(LogUtils.AGENT_ID + "request to server failed: \r\n {}", 
                LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("retcode", AgentErrorCode.NETWORK_ERROR);
        resultMap.put("message", "Request to  AgentServer failed");
        return resultMap;
    }
    
    /**
     * 返回结果不正确
     * @return
     */
    private static Map<String, Object> returnContentError()
    {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("retcode", AgentErrorCode.RETURN_CONTENT_ERROR);
        resultMap.put("message", "The AgentServer return null");
        return resultMap;
    }
    
    /**
     * 格式化cookie
     * @param cookies
     * @return
     */
    private static String formatCookie(String cookies)
    {
        if (null == cookies)
        {
            return "";
        }
        cookies = Normalizer.normalize(cookies, Form.NFKC);
        String replaceAll = cookies.replaceAll("\r", "")
             .replaceAll("\n", "");
        return replaceAll;
    }
    
    /**
     * 格式化头域值；format header
     * @param header
     * @return
     */
    private static String formatHeader(String header)
    {
        if (null == header)
        {
            return "";
        }
        header = Normalizer.normalize(header, Form.NFKC);
        String replaceAll = header.replaceAll("\r", "")
             .replaceAll("\n", "")
             .replaceAll(":", "")
             .replaceAll("=", "");
        return replaceAll;
    }
}
