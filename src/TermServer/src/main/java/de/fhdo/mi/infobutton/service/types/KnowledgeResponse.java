package de.fhdo.mi.infobutton.service.types;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.hl7.AggregateKnowledgeResponse;

/**
 *
 * @author Robert Mützner <robert.muetzner@fh-dortmund.de>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "knowledgeResponse")
@XmlRootElement
public class KnowledgeResponse
{
  @XmlElement(required = true)
  private AggregateKnowledgeResponse knowledgeResponse;

  /**
   * @return the knowledgeResponse
   */
  public AggregateKnowledgeResponse getKnowledgeResponse()
  {
    return knowledgeResponse;
  }

  /**
   * @param knowledgeResponse the knowledgeResponse to set
   */
  public void setKnowledgeResponse(AggregateKnowledgeResponse knowledgeResponse)
  {
    this.knowledgeResponse = knowledgeResponse;
  }
  
}
