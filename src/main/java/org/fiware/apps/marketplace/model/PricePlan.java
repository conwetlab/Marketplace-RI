package org.fiware.apps.marketplace.model;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2015 CoNWeT Lab, Universidad Politécnica de Madrid
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

import java.util.Set;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonProperty;
import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;

@Entity
@Table(name = "price_plans")
@XmlRootElement(name = "price_plan")
@IgnoreMediaTypes("application/*+json")
public class PricePlan {
	
	// Hibernate
	private int id;
	private Offering offering;
	
	private String title;
	private String comment;
	private Set<PriceComponent> priceComponents;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@XmlTransient
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "offering", nullable=false)
	public Offering getOffering() {
		return offering;
	}

	public void setOffering(Offering offering) {
		this.offering = offering;
	}

	@XmlElement
	@Column(name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@XmlElement
	@Column(name = "comment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@XmlElement(name = "priceComponent")
    @JsonProperty("priceComponents")
	@OneToMany(mappedBy = "pricePlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	public Set<PriceComponent> getPriceComponents() {
		return priceComponents;
	}

	public void setPriceComponents(Set<PriceComponent> priceComponents) {
		this.priceComponents = priceComponents;
	}

	/**
	 * This object is MUTABLE since user can modify its attributes at ANY TIME. Please, be careful and
	 * set all the properties before including an instance in any collection or you may experience
	 * unexpected behaviors.
	 */
	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 1;
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (priceComponents == null ? 0 : priceComponents.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) {
			return true;
		}
		
		if (obj instanceof PricePlan) {
		
			PricePlan other = (PricePlan) obj;
			return this.title.equals(other.title) && 
					this.comment.equals(other.comment) &&
					this.priceComponents.equals(other.priceComponents);		
		}
		
		return false;
	}
	
	
}
