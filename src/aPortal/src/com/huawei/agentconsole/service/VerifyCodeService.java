
package com.huawei.agentconsole.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.common.util.ValidateUtils;
import com.huawei.agentconsole.ws.param.RestResponse;

/**
 * 
 * <p>Title: 获取验证码  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年8月25日
 * @since
 */
public class VerifyCodeService
{
    private final static int VERIFYCODE_IDISTACNE = 0;
    
    private final static int VERIFYCODE_IDISTORT = 0;

    private final static int VERIFYCODE_IHEIGHT = 35;
    
    private static Logger log = LoggerFactory.getLogger(VerifyCodeService.class);
    
    private static SecureRandom random = new SecureRandom();

    private static String bVariableFont;

    private static int iWidth;

    private static int iHeight = 35;

    private static int iMinFontSize = 30;

    private static int iMaxFontSize;

    private static String bVariableFontSize;

    private static String bIsContainLowercase;

    private static String bIsContainUppercase;

    private static String dictionary;
    private static String bIsRotate;

    private static int iDistance;

    private static String bIsSetBackground;

    private static String bIsSetInterferon;

    private static int iDistort;

    private static String[] fontsType;

    private static int iCharsLen = 4;

    private static double[] rotateRange = {-0.3D, 0.3D};

    private String agentId;
    
    public VerifyCodeService(String agentId)
    {
        this.agentId = agentId;
    }
    
    public static void init()
    {
        bVariableFont = ConfigProperties.getKey(ConfigList.VERIFY,
                "VERIFYCODE_BVARIABLEFONT");
        bVariableFontSize = ConfigProperties.getKey(ConfigList.VERIFY,
                "VERIFYCODE_BVARIABLEFONTSIZE");
        bIsContainLowercase = ConfigProperties.getKey(ConfigList.VERIFY,
                "VERIFYCODE_BISCONTAINLOWERCASE");
        bIsContainUppercase = ConfigProperties.getKey(ConfigList.VERIFY,
                "VERIFYCODE_BISCONTAINUPPERCASE");
        bIsRotate = ConfigProperties.getKey(ConfigList.VERIFY, "VERIFYCODE_BISROTATE");
        
        try
        {
            iDistance = Integer.parseInt(ConfigProperties.getKey(ConfigList.VERIFY,
                    "VERIFYCODE_IDISTACNE"));
        }
        catch (NumberFormatException e)
        {
            iDistance = VERIFYCODE_IDISTACNE;
        }
        
        bIsSetBackground = ConfigProperties.getKey(ConfigList.VERIFY,
                "VERIFYCODE_BISSETBACKGROUND");
        bIsSetInterferon = ConfigProperties.getKey(ConfigList.VERIFY,
                "VERIFYCODE_BISSETINTERFERON");
        
        try
        {
            iDistort = Integer.parseInt(ConfigProperties.getKey(ConfigList.VERIFY,
                    "VERIFYCODE_IDISTORT"));
        }
        catch (NumberFormatException e)
        {
            iDistort = VERIFYCODE_IDISTORT;
        }
        
        try
        {
            iHeight = Integer.parseInt(ConfigProperties.getKey(ConfigList.VERIFY,
                    "VERIFYCODE_IHEIGHT"));
        }
        catch (NumberFormatException e)
        {
            iHeight = VERIFYCODE_IHEIGHT;
        }
        
        dictionary = "0123456789";

        if (bIsContainLowercase.equalsIgnoreCase("true"))
        {
            if (bIsContainUppercase.equalsIgnoreCase("true"))
            {
                dictionary = "2345689ABCDEFGHLRTYabcdefht";
            }

            else
            {
                dictionary = "2345689abcdefhkstwx";
            }

        }
        else if (bIsContainUppercase.equalsIgnoreCase("true"))
        {
            dictionary = "12345689ABCDEFGHJKLPRSTWXY";
        }

        fontsType = new String[]{"Arial", "Courier", "Courier New",
                "Times New Roman", "SansSerif", "Monospaced", "Verdana",
                "Microsoft Sans Serif", "Comic Sans MS"};

        if (iDistance == 0)
        {
            iDistance = 1;
        }

    }

    /**
     * 获取颜色
     * @param colorRange
     * @return
     */
    private static Color getColor(int[] colorRange)
    {
        int r = getRandomInRange(colorRange);
        int g = getRandomInRange(colorRange);
        int b = getRandomInRange(colorRange);
        return new Color(r, g, b);
    }

    /**
     * 获取随机数
     * @param range
     * @return
     */
    private static int getRandomInRange(int[] range)
    {
        if ((range == null) || (range.length != 2))
        {
            return -1;
        }
        return (int) (random.nextDouble() * (range[1] - range[0]) + range[0]);
    }

    /**
     * 获取随机数
     * @param range
     * @return
     */
    private static double getRandomInRange(double[] range)
    {
        if ((null == range) || (range.length != 2))
        {
            return -1;
        }
        return random.nextDouble() * (range[1] - range[0]) + range[0];
    }

    /**
     * 生成字符串
     * @param dictionary
     * @return
     */
    private static String generateCodeString(String dictionary)
    {
        iWidth = (int) (iHeight * 3.0D * iCharsLen / 4.0D);
        iMaxFontSize = 16 * iHeight / 12;

        char[] dictionaryChars = dictionary.toCharArray();
        char[] codeChars = new char[iCharsLen];
        int dicLen = dictionaryChars.length;

        for (int i = 0; i < iCharsLen; i++)
        {
            int index = random.nextInt(dicLen);
            codeChars[i] = dictionaryChars[index];
        }

        return String.valueOf(codeChars, 0, codeChars.length);
    }

    /**
     * 随机格式
     * @param fontsType
     * @return
     */
    public static Font[] generateCodeFonts(String[] fontsType)
    {
        Font[] chosenFonts = new Font[iCharsLen];
        String sFont = null;
        int fontSize = -1;
        for (int i = 0; i < iCharsLen; i++)
        {
            sFont = fontsType[(random.nextInt(3) % 4)];
            fontSize = random.nextInt(iMaxFontSize)
                    % (iMaxFontSize - iMinFontSize + 1) + iMinFontSize;

            chosenFonts[i] = new Font(sFont, 0, fontSize);
        }

        return chosenFonts;
    }

    private static BufferedImage renderWord(String word, int width, int height)
    {
        int charSpace = iDistance - 2;

        BufferedImage image = new BufferedImage(iWidth, iHeight, 2);

        setBackground(image);
        Graphics2D g2D = image.createGraphics();
        g2D.getDeviceConfiguration().createCompatibleImage(iHeight, iHeight, 3);

        g2D.dispose();

        g2D = image.createGraphics();

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        g2D.setRenderingHints(hints);

        FontRenderContext frc = g2D.getFontRenderContext();
        char[] wordChars = word.toCharArray();
        int len = wordChars.length;
        Font[] chosenFonts = new Font[len];
        int[] charWidths = new int[len];
        int fontSize = 0;
        int widthNeeded = 0;
        String sFont = fontsType[0];

        fontSize = iHeight + 10;

        for (int i = 0; i < len; i++)
        {
            if (bVariableFont.equalsIgnoreCase("true"))
            {
                sFont = fontsType[(random.nextInt(3) % 4)];
            }

            if (bVariableFontSize.equalsIgnoreCase("true"))
            {
                fontSize = random.nextInt(iMaxFontSize)
                        % (iMaxFontSize - iMinFontSize + 1) + iMinFontSize;
            }

            chosenFonts[i] = new Font(sFont, 0, fontSize);

            char[] charToDraw = {wordChars[i]};
            GlyphVector gv = chosenFonts[i].createGlyphVector(frc, charToDraw);
            charWidths[i] = (int) gv.getVisualBounds().getWidth();

            widthNeeded += charWidths[i];
        }

        int startPosX = (iWidth - widthNeeded) / 5;

        for (int i = 0; i < wordChars.length; i++)
        {
            AffineTransform affineTransform = new AffineTransform();

            double rotateX = 0.0D;

            if ("true".equals(bIsRotate))
            {
                rotateX = getRandomInRange(rotateRange);
            }

            affineTransform.rotate(rotateX, 15.0D, 15.0D);
            g2D.setTransform(affineTransform);

            g2D.setFont(chosenFonts[i]);

            Color color = getRandColor(0, 120);
            g2D.setColor(color);

            int startPosY = (int) (iHeight / 2.0F + chosenFonts[i].getLineMetrics(
                    Character.toString(wordChars[i]), frc).getAscent() / 2.0F);

            g2D.drawString(Character.toString(wordChars[i]), startPosX,
                    startPosY);

            startPosX = startPosX + charWidths[i] + charSpace;
        }

        g2D.dispose();
        return image;
    }

    private static BufferedImage setBackground(BufferedImage image)
    {
        Graphics2D g2D = image.createGraphics();
        int width = iWidth + 3 * iDistance;
        int height = iHeight;

        int l_iTmp_0 = 0;
        int l_iTmp_1 = 0;
        int l_iWidth = iWidth;
        int l_iHeight = iHeight;

        g2D.fillRect(0, 0, width, height);

        g2D.setColor(Color.white);
        g2D.drawRect(0, 0, width, height);

        if ("true".equals(bIsSetBackground))
        {
            int[] disturbColor = {0, 255};

            for (int i = 0; i < 150; i++)
            {
                int xs = random.nextInt(width);
                int ys = random.nextInt(height);
                int xe = xs;
                int ye = ys;
                Color fgColor = getColor(disturbColor);
                g2D.setColor(fgColor);
                g2D.drawLine(xs, ys, xe, ye);
            }

        }

        if ("true".equals(bIsSetInterferon))
        {
            l_iTmp_0 = l_iWidth + 1;
            l_iTmp_1 = l_iHeight + 1;

            int times_1 = iHeight / 10;
            int times_2 = iWidth / 10;

            for (int ii = 0; ii < times_1; ii++)
            {
                g2D.setStroke(new BasicStroke(
                        (float) ((createRandom(30) + 1) / 16.0D)));

                g2D.setColor(getRandColor(50, 200));
                g2D.drawLine(0, createRandom(l_iTmp_1), l_iWidth,
                        createRandom(l_iTmp_1));
            }

            for (int ii = 0; ii < times_2; ii++)
            {
                g2D.setStroke(new BasicStroke(
                        (float) ((createRandom(30) + 1) / 16.0D)));

                g2D.setColor(getRandColor(50, 200));
                g2D.drawLine(createRandom(l_iTmp_0), 0, createRandom(l_iTmp_0),
                        l_iHeight);
            }

        }

        g2D.dispose();

        return image;
    }

    /**
     * 随机数生成
     * @param iSeed
     * @return
     */
    private static int createRandom(int iSeed)
    {
        int l_iValue = 0;

        byte[] l_pbtRand = new byte[4];

        random.nextBytes(l_pbtRand);

        for (int ii = 0; ii < 4; ii++)
        {
            byte l_btTmp = l_pbtRand[ii];
            l_iValue += ((l_btTmp & 0xFF) << 8 * ii);
        }
        return Math.abs(l_iValue % iSeed);
    }

    private static Color getRandColor(int iFC, int iBC)
    {
        int l_iMax = Math.max(iFC, iBC);
        int l_iMin = Math.min(iFC, iBC);

        if (l_iMin == l_iMax)
        {
            return new Color(l_iMin, l_iMin, l_iMin);
        }

        l_iMax -= l_iMin - 1;

        return new Color(l_iMin + createRandom(l_iMax), l_iMin
                + createRandom(l_iMax), l_iMin + createRandom(l_iMax));
    }

    private static BufferedImage distortImage(BufferedImage oldImage,
            int iMargin)
    {
        double dPhase = random.nextInt(6);

        BufferedImage newImage = new BufferedImage(oldImage.getWidth(),
                oldImage.getHeight(), 1);

        int width = newImage.getWidth();
        int height = newImage.getHeight();

        Graphics graphics = newImage.getGraphics();

        graphics.setColor(Color.white);

        graphics.fillRect(0, 0, width, height);

        graphics.dispose();

        if (iMargin > 4)
        {
            iMargin = random.nextInt(iMargin) % (iMargin - 4) + 5;
        }

        double dLen = height;

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                
                double x = 3.141592653589793D * j / dLen;
                x += dPhase;
                double dy = Math.sin(x);

                int oX = 0;
                int oY = 0;
                oX = i + (int) (dy * iMargin);
                oY = j;
                int rgb = oldImage.getRGB(i, j);

                if ((oX >= 0) && (oX < width) && (oY >= 0) && (oY < height))
                {
                    newImage.setRGB(oX, oY, rgb);
                }
            }
        }
        return newImage;
    }



    /**
     * 获取验证码
     * @throws CommonException 
     */
    public RestResponse getVerifyCode(HttpServletRequest request) 
    {
        RestResponse restResponse = new RestResponse();
        if (StringUtils.isNullOrEmpty(agentId) || !ValidateUtils.isAgentId(agentId))
        {
            restResponse.setMessage("agentId is invalid ");
            restResponse.setReturnCode(AgentErrorCode.AGENT_REST_INVALID);
            return restResponse;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        boolean needVerifyCode = true;
        String needVerifyCodeKey = agentId + CommonConstant.IS_NEED_VERIFY;
        if ("true".equalsIgnoreCase(ConfigProperties.getKey(ConfigList.VERIFY, "VERIFYCODE_ISUSED")))
        {
            //启用验证码功能
            ServletContext servletCtx = request.getSession().getServletContext();
            Date lastUpdateTime = (Date)servletCtx.getAttribute(needVerifyCodeKey);
            if (null == lastUpdateTime) 
            {
                needVerifyCode = false;
            }
        }
        else
        {
            needVerifyCode = false;
        }
        restResponse.setReturnCode(AgentErrorCode.SUCCESS);
        result.put("needVerifyCode", needVerifyCode);
        if (needVerifyCode)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();  
            try
            {
                String sRand = generateCodeString(dictionary); //获取验证码
                BufferedImage image = renderWord(sRand, iWidth, iHeight);
                image = distortImage(image, iDistort);
                ImageIO.setUseCache(false);
                ImageIO.write(image, "JPEG", baos);
                byte[] bytes = baos.toByteArray();   
                result.put("imageData", new String(Base64.encodeBase64(bytes), CommonConstant.UTF_8));
                request.getSession().setAttribute("verifyCode", sRand);
            }
            catch (IOException e)
            {
                restResponse.setReturnCode(AgentErrorCode.CREATE_VERIFYCODE_FAILED);
                log.error("getVerifyCode, IOException.\r\n {}, ",
                        new Object[]{
                                LogUtils.encodeForLog(e.getMessage())});
                restResponse.setMessage("create verifycode failed");
                restResponse.setReturnCode(AgentErrorCode.CREATE_VERIFYCODE_FAILED);
            }
            finally
            {
                
                try
                {
                    baos.close();
                }
                catch (IOException e)
                {
                    log.error("getVerifyCode, IOException.\r\n {}, ",
                            new Object[]{
                                    LogUtils.encodeForLog(e.getMessage())});
                }
                
            }
        }
        restResponse.setRetObject("result", result);
        return restResponse;
    }
}
