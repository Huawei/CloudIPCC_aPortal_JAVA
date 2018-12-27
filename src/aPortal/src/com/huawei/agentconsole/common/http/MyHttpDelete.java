package com.huawei.agentconsole.common.http;

import java.net.URI;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;


public class MyHttpDelete extends HttpEntityEnclosingRequestBase 
{
	
	private static final String METHOD_NAME ="DELETE";

	@Override
	public String getMethod() {
		return METHOD_NAME;
	}

	public MyHttpDelete(final String uri) {
        super();
        String normalize = Normalizer.normalize(uri, Form.NFKC);
        setURI(URI.create(normalize));
    }

    public MyHttpDelete(final URI uri) {
        super();
        setURI(uri);
    }

    public MyHttpDelete() {
        super();
    }

}
