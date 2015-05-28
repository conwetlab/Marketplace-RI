package org.fiware.apps.marketplace.model;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2014-2015 CoNWeT Lab, Universidad Politécnica de Madrid
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonProperty;
import org.fiware.apps.marketplace.utils.xmladapters.StoreXMLAdapter;
import org.fiware.apps.marketplace.utils.xmladapters.UserXMLAdapter;
import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;

@Entity
// Each offerings description has to be unique for a store context
@Table(name = "descriptions", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "store" }) })
@XmlRootElement(name = "description")
@IgnoreMediaTypes("application/*+json")
public class Description {

	private Integer id;
	private String url;
	private String name;
	private String displayName;
	private String comment;
	private Date registrationDate;
	private Store store;
	private User lasteditor;	
	private User creator;
	private List<Offering> offerings;
	
	public Description() {
		this.offerings = new ArrayList<Offering>();
	}
	
	public Description(MinifiedDescription minDescription) {
		this.name = minDescription.getName();
		this.store = minDescription.getStore();
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlID
	@XmlAttribute 
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement
	@Column(name = "display_name")
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@XmlElement
	@Column(name = "url", nullable = false)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement
	@Column(name = "comment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@XmlElement
	@XmlJavaTypeAdapter(StoreXMLAdapter.class)
	@ManyToOne(optional = false)
	@JoinColumn(name = "store", nullable=false)
	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	@XmlElement
	@XmlJavaTypeAdapter(UserXMLAdapter.class)
	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", nullable=false)
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	@XmlElement
	@XmlJavaTypeAdapter(UserXMLAdapter.class)
	@ManyToOne(optional = false)
	@JoinColumn(name = "last_editor", nullable=false)
	public User getLasteditor() {
		return lasteditor;
	}

	public void setLasteditor(User lasteditor) {
		this.lasteditor = lasteditor;
	}

	@XmlElement
	@Column(name = "registration_date")
	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

    @XmlElement(name = "offering")
    @JsonProperty("offerings")
	@OneToMany(mappedBy = "describedIn", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	public List<Offering> getOfferings() {
		return offerings;
	}

	public void setOfferings(List<Offering> offerings) {
		this.offerings = offerings;
	}
	
	public void addOffering(Offering offering) {
		this.offerings.add(offering);
	}
	
	public void addOfferings(Collection<Offering> offerings) {
		this.offerings.addAll(offerings);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((store == null) ? 0 : store.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj instanceof Description) {
			Description other = (Description) obj;
			
			// Avoid null pointer exceptions...
			if (this.name == null || this.store == null) {
				return false;
			}
			
			if (this.name.equals(other.name) && this.store.equals(other.store)) {
				return true;
			}
		}
		
		return false;
	}
}
