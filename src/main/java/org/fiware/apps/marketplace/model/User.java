package org.fiware.apps.marketplace.model;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2014 CoNWeT Lab, Universidad Politécnica de Madrid
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fiware.apps.marketplace.utils.xmladapters.HiddenFieldsXMLAdapter;
import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;

@Entity
@Table(name = "users")
@XmlRootElement(name = "user")
@IgnoreMediaTypes("application/*+json")
public class User {
	
	private Integer id;
	private String userName;
	private String displayName;
	private String password;
	private String email;
	private Date registrationDate;
	private String company;
	private boolean oauth2 = false;		// False by default
	// This lists are needed to allow cascade deletion
	private List<Store> storesCreated;
	private List<Store> storesModified;
	private List<Description> descriptionsCreated;
	private List<Description> descriptionsModified;
	private List<Offering> bookmarks;
		
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
	@Column(name = "user_name", unique = true, nullable = false)
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
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
	// Avoid returning the mail in the API
	@XmlJavaTypeAdapter(HiddenFieldsXMLAdapter.class)
	@Column(name = "email", unique = true, nullable = false)
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@XmlElement
	// Avoid returning the password in the API
	// Encrypt the password received through the API
	@XmlJavaTypeAdapter(HiddenFieldsXMLAdapter.class)
	@Column(name = "password", nullable = false)
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		// The password is encoded using the user name as salt
		this.password = password;
	}
	
	@XmlElement
	@Column(name = "registration_date", nullable = false)
	public Date getRegistrationDate() {
		return registrationDate;
	}
	
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}
	
	@XmlElement
	@Column(name = "company")
	public String getCompany() {
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	@XmlTransient
	@Column(name = "oauth2")
	public boolean isOauth2() {
		return oauth2;
	}

	public void setOauth2(boolean oauth2) {
		this.oauth2 = oauth2;
	}

	@XmlTransient
	@OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public List<Store> getStoresCreated() {
		return storesCreated;
	}

	public void setStoresCreated(List<Store> storesCreated) {
		this.storesCreated = storesCreated;
	}

	@XmlTransient
	@OneToMany(mappedBy = "lasteditor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public List<Store> getStoresModified() {
		return storesModified;
	}

	public void setStoresModified(List<Store> storesModified) {
		this.storesModified = storesModified;
	}

	@XmlTransient
	@OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public List<Description> getDescriptionsCreated() {
		return descriptionsCreated;
	}

	public void setDescriptionsCreated(List<Description> descriptionsCreated) {
		this.descriptionsCreated = descriptionsCreated;
	}

	@XmlTransient
	@OneToMany(mappedBy = "lasteditor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public List<Description> getDescriptionsModified() {
		return descriptionsModified;
	}

	public void setDescriptionsModified(List<Description> descriptionsModified) {
		this.descriptionsModified = descriptionsModified;
	}

	@XmlTransient
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "bookmarks", 
		      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
		      inverseJoinColumns = {@JoinColumn(name = "offering_id", referencedColumnName = "id")})
	public List<Offering> getBookmarks() {
		return bookmarks;
	}

	public void setBookmarks(List<Offering> bookmarks) {
		this.bookmarks = bookmarks;
	}

	@Override
	public int hashCode() {
		return this.id == null ? 0 : this.id;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) {
			return false;
		}
		
		if (obj instanceof User) {
			User other = (User) obj;
			return other.id == this.id;
		} else {
			return false;
		}
	}

}
