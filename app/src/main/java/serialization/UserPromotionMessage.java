package serialization;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by edgar on 10/2/2015.
 */
public class UserPromotionMessage implements Serializable {
    private static final long serialVersionUID=2000L;

    public int result;
    public String promoDescription;
    public Date promoExpirationDate;
    public String clientName ;
    public byte[] clientLogo;
    public String iconFileName;
    public byte[] promotionalPicture;
    public String promotionalPictureName;
}
