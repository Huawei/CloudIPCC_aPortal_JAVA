
package javax.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * <p>Title: Resteasy升级到3.0.16后，jaxrs-api-*.Final.jar
 *  被 jboss-jaxrs-api_2.0_spec-1.0.0.Final.jar替换，但是该jar中没有该类，所以进行添加</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2017年4月13日
 * @since
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority
{
  public abstract int value();
}
