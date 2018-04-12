//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.20 um 10:44:07 AM CEST 
//


package ehd._001;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ehd._001 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Fkey_QNAME = new QName("urn:ehd/001", "fkey");
    private final static QName _Keytab_QNAME = new QName("urn:ehd/001", "keytab");
    private final static QName _Keytabs_QNAME = new QName("urn:ehd/001", "keytabs");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ehd._001
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FkeyTyp }
     * 
     */
    public FkeyTyp createFkeyTyp() {
        return new FkeyTyp();
    }

    /**
     * Create an instance of {@link KeytabTyp }
     * 
     */
    public KeytabTyp createKeytabTyp() {
        return new KeytabTyp();
    }

    /**
     * Create an instance of {@link KeytabsTyp }
     * 
     */
    public KeytabsTyp createKeytabsTyp() {
        return new KeytabsTyp();
    }

    /**
     * Create an instance of {@link Key }
     * 
     */
    public Key createKey() {
        return new Key();
    }

    /**
     * Create an instance of {@link KeyTyp }
     * 
     */
    public KeyTyp createKeyTyp() {
        return new KeyTyp();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FkeyTyp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ehd/001", name = "fkey")
    public JAXBElement<FkeyTyp> createFkey(FkeyTyp value) {
        return new JAXBElement<FkeyTyp>(_Fkey_QNAME, FkeyTyp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeytabTyp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ehd/001", name = "keytab")
    public JAXBElement<KeytabTyp> createKeytab(KeytabTyp value) {
        return new JAXBElement<KeytabTyp>(_Keytab_QNAME, KeytabTyp.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeytabsTyp }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:ehd/001", name = "keytabs")
    public JAXBElement<KeytabsTyp> createKeytabs(KeytabsTyp value) {
        return new JAXBElement<KeytabsTyp>(_Keytabs_QNAME, KeytabsTyp.class, null, value);
    }

}
