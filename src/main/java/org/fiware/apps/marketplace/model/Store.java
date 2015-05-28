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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fiware.apps.marketplace.utils.xmladapters.UserXMLAdapter;
import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;


@Entity
@Table(name = "stores")
@XmlRootElement(name = "store")
@IgnoreMediaTypes("application/*+json")
public class Store {
	
	private Integer id;
	private String url;
	private String displayName;
	private String name;
	private String comment;
	private Date registrationDate;
	private List<Description> descriptions;
	private User lasteditor;	
	private User creator;
	
	// Image
	private String imagePath;
	private String imageBase64;
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@XmlTransient
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer  id) {
		this.id = id;
	}
	
	@XmlID
	@XmlAttribute 
	@Column(name = "name", unique = true, nullable = false)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlID
	@XmlElement 
	@Column(name = "display_name", unique = true, nullable = false)
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	@XmlElement
	@Column(name = "url", unique = true, nullable = false)
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
	@JoinColumn(name = "last_editor", nullable = false)
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
	
	@XmlElement
	@Transient
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	// The image in B64 won't be stored in the database
	// StoreBo will transform the b64 into bytes and will store the image in the disk
	// Only PNG accepted
	@XmlElement
	@Transient
	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	@XmlTransient
	@OneToMany(mappedBy="store", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	public List<Description> getDescriptions() {
		return descriptions;
	}
	
	public void setDescriptions(List<Description> offeringsDescriptions) {
		this.descriptions = offeringsDescriptions;
	}
	
	public void addDescription(Description description) {
		this.descriptions.add(description);
	}
	
	public void removeDescription(Description description) {
		this.descriptions.remove(description);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj instanceof Store) {
			Store other = (Store) obj;
			
			// Avoid null pointer exceptions...
			if (this.name == null) {
				return false;
			}
			
			if (this.name.equals(other.name)) {
				return true;
			}			
		}
	
		return false;
	}
	
	
}
