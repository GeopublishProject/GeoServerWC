package serialization;

import java.io.Serializable;

/**
 * Created by edgar on 10/2/2015.
 */
public class PromoMessage implements Serializable {
    private static final long serialVersionUID=800828L;

    public int action;
    public String deviceId;
    public String code;


}
