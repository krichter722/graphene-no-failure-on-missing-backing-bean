package richtercloud.graphene.no.failure.on.missing.backing.bean;

import javax.faces.bean.ManagedBean;

/**
 *
 * @author richter
 */
@ManagedBean
public class ConstantsBean {
    public final static String THE_KEY = "theKey";

    public ConstantsBean() {
    }

    public String getSessionKey() {
        return THE_KEY;
    }
}
