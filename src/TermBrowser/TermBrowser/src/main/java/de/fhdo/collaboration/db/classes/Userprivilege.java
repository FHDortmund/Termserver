package de.fhdo.collaboration.db.classes;
// Generated 30.06.2015 09:32:45 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Userprivilege generated by hbm2java
 */
@Entity
@Table(name = "userprivilege"
)
public class Userprivilege implements java.io.Serializable
{

  private Long id;
  private Collaborationuser collaborationuser;
  private Role role;
  private String contentType;
  private Long objectId;
  private Long objectVersionId;
  private Boolean sendMail;

  public Userprivilege()
  {
  }

  public Userprivilege(Collaborationuser collaborationuser, Role role, String contentType)
  {
    this.collaborationuser = collaborationuser;
    this.role = role;
    this.contentType = contentType;
  }

  public Userprivilege(Collaborationuser collaborationuser, Role role, String contentType, Long objectId, Long objectVersionId, Boolean sendMail)
  {
    this.collaborationuser = collaborationuser;
    this.role = role;
    this.contentType = contentType;
    this.objectId = objectId;
    this.objectVersionId = objectVersionId;
    this.sendMail = sendMail;
  }

  @Id
  @GeneratedValue(strategy = IDENTITY)

  @Column(name = "id", unique = true, nullable = false)
  public Long getId()
  {
    return this.id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "collaborationuser_id", nullable = false)
  public Collaborationuser getCollaborationuser()
  {
    return this.collaborationuser;
  }

  public void setCollaborationuser(Collaborationuser collaborationuser)
  {
    this.collaborationuser = collaborationuser;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", nullable = false)
  public Role getRole()
  {
    return this.role;
  }

  public void setRole(Role role)
  {
    this.role = role;
  }

  @Column(name = "contentType", nullable = false, length = 30)
  public String getContentType()
  {
    return this.contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  @Column(name = "objectId")
  public Long getObjectId()
  {
    return this.objectId;
  }

  public void setObjectId(Long objectId)
  {
    this.objectId = objectId;
  }

  @Column(name = "objectVersionId")
  public Long getObjectVersionId()
  {
    return this.objectVersionId;
  }

  public void setObjectVersionId(Long objectVersionId)
  {
    this.objectVersionId = objectVersionId;
  }

  @Column(name = "sendMail")
  public Boolean getSendMail()
  {
    return this.sendMail;
  }

  public void setSendMail(Boolean sendMail)
  {
    this.sendMail = sendMail;
  }
  
  
  @Transient
  public boolean getAdminRights()
  {
    if((role.getMayAdminProposal() != null && role.getMayAdminProposal().booleanValue()) ||
       (role.getAdminFlag() != null && role.getAdminFlag().booleanValue()))
    {
      return true;
    }
    return false;
  }

}
