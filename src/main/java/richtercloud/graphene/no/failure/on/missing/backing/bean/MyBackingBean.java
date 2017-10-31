package richtercloud.graphene.no.failure.on.missing.backing.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

/**
 *
 * @author richter
 */
@ManagedBean
public class MyBackingBean {

    public String theAction() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstantsBean.THE_KEY, "abc");
        return "index.xhtml?faces-redirect=true";
    }
}
