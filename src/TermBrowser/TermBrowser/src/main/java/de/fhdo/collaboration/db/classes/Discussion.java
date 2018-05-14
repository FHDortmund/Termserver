package de.fhdo.collaboration.db.classes;
// Generated 30.06.2015 09:32:45 by Hibernate Tools 4.3.1


import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Discussion generated by hbm2java
 */
@Entity
@Table(name="discussion"
    
)
public class Discussion  implements java.io.Serializable {


     private Long id;
     private Collaborationuser collaborationuser;
     private Proposal proposal;
     private Proposalobject proposalobject;
     private Long lastDiscussionId;
     private Date date;
     private Date changed;
     private Boolean initial;
     private String longDescription;
     private String shortDescription;
     private Integer postNumber;
     private Set<Link> links = new HashSet<Link>(0);
     private Set<Rating> ratings = new HashSet<Rating>(0);
     private Set<Quote> quotesForDiscussionId = new HashSet<Quote>(0);
     private Set<Quote> quotesForDiscussionIdQuoted = new HashSet<Quote>(0);

    public Discussion() {
    }

    public Discussion(Collaborationuser collaborationuser, Proposal proposal, Proposalobject proposalobject, Long lastDiscussionId, Date date, Date changed, Boolean initial, String longDescription, String shortDescription, Integer postNumber, Set<Link> links, Set<Rating> ratings, Set<Quote> quotesForDiscussionId, Set<Quote> quotesForDiscussionIdQuoted) {
       this.collaborationuser = collaborationuser;
       this.proposal = proposal;
       this.proposalobject = proposalobject;
       this.lastDiscussionId = lastDiscussionId;
       this.date = date;
       this.changed = changed;
       this.initial = initial;
       this.longDescription = longDescription;
       this.shortDescription = shortDescription;
       this.postNumber = postNumber;
       this.links = links;
       this.ratings = ratings;
       this.quotesForDiscussionId = quotesForDiscussionId;
       this.quotesForDiscussionIdQuoted = quotesForDiscussionIdQuoted;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)

    
    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="collaborationUserId")
    public Collaborationuser getCollaborationuser() {
        return this.collaborationuser;
    }
    
    public void setCollaborationuser(Collaborationuser collaborationuser) {
        this.collaborationuser = collaborationuser;
    }

@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="proposalId")
    public Proposal getProposal() {
        return this.proposal;
    }
    
    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="proposalObjectId")
    public Proposalobject getProposalobject() {
        return this.proposalobject;
    }
    
    public void setProposalobject(Proposalobject proposalobject) {
        this.proposalobject = proposalobject;
    }

    
    @Column(name="lastDiscussionId")
    public Long getLastDiscussionId() {
        return this.lastDiscussionId;
    }
    
    public void setLastDiscussionId(Long lastDiscussionId) {
        this.lastDiscussionId = lastDiscussionId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date", length=19)
    public Date getDate() {
        return this.date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="changed", length=19)
    public Date getChanged() {
        return this.changed;
    }
    
    public void setChanged(Date changed) {
        this.changed = changed;
    }

    
    @Column(name="initial")
    public Boolean getInitial() {
        return this.initial;
    }
    
    public void setInitial(Boolean initial) {
        this.initial = initial;
    }

    
    @Column(name="longDescription", length=65535)
    public String getLongDescription() {
        return this.longDescription;
    }
    
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    
    @Column(name="shortDescription", length=65535)
    public String getShortDescription() {
        return this.shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    
    @Column(name="postNumber")
    public Integer getPostNumber() {
        return this.postNumber;
    }
    
    public void setPostNumber(Integer postNumber) {
        this.postNumber = postNumber;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="discussion")
    public Set<Link> getLinks() {
        return this.links;
    }
    
    public void setLinks(Set<Link> links) {
        this.links = links;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="discussion")
    public Set<Rating> getRatings() {
        return this.ratings;
    }
    
    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="discussionByDiscussionId")
    public Set<Quote> getQuotesForDiscussionId() {
        return this.quotesForDiscussionId;
    }
    
    public void setQuotesForDiscussionId(Set<Quote> quotesForDiscussionId) {
        this.quotesForDiscussionId = quotesForDiscussionId;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="discussionByDiscussionIdQuoted")
    public Set<Quote> getQuotesForDiscussionIdQuoted() {
        return this.quotesForDiscussionIdQuoted;
    }
    
    public void setQuotesForDiscussionIdQuoted(Set<Quote> quotesForDiscussionIdQuoted) {
        this.quotesForDiscussionIdQuoted = quotesForDiscussionIdQuoted;
    }




}

