package org.fiware.apps.marketplace.dao.impl;

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

import java.util.List;

import org.fiware.apps.marketplace.dao.UserDao;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.User;
import org.fiware.apps.marketplace.utils.MarketplaceHibernateDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("userDao")
public class UserDaoImpl  extends MarketplaceHibernateDao implements UserDao {
	
	private static final String TABLE_NAME = User.class.getName();

	@Override
	@Transactional(readOnly = false)
	public void save(User user) {
		getSession().saveOrUpdate(user);	
	}

	@Override
	@Transactional(readOnly = false)
	public void update(User user) {
		// Avoid NonUniqueObjectException. This exception is risen because
		// the user is retrieved twice (one because of the authentication and
		// another one because of the update) and the first time is detached
		getSession().merge(user);
	}

	@Override
	@Transactional(readOnly = false)
	public void delete(User user) {
		getSession().delete(user);
	}

	@Override
	@Transactional(readOnly = true)
	public User findByName(String userName) throws UserNotFoundException{
		String query = String.format("from %s where userName=:userName", TABLE_NAME);
		List<?> list = getSession()
				.createQuery(query)
				.setParameter("userName", userName)
				.list();
		
		if (list.isEmpty()) {
			throw new UserNotFoundException("User " + userName + " not found");
		} else {
			return (User) list.get(0);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public User findByEmail(String email) throws UserNotFoundException{
		String query = String.format("from %s where email=:email", TABLE_NAME);
		List<?> list = getSession()
				.createQuery(query)
				.setParameter("email", email)
				.list();
		
		if (list.isEmpty()) {
			throw new UserNotFoundException("User with email" + email + " not found");
		} else {
			return (User) list.get(0);
		}
	}
	
	@Override
	public boolean isUserNameAvailable(String userName) {
		
		boolean available = false;
		
		try {
			findByName(userName);
		} catch (UserNotFoundException e) {
			available = true;
		}
		
		return available;
	}
	
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        String query = String.format("from %s where email=:email", TABLE_NAME);

        List<?> list = getSession()
                .createQuery(query)
                .setParameter("email", email)
                .list();

        return list.isEmpty();
    }
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<User> getUsersPage(int offset, int max) {
		return getSession()
				.createCriteria(User.class)
				.setFirstResult(offset)
				.setMaxResults(max)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<User> getAllUsers() {
		return getSession()
				.createCriteria(User.class)
				.list();
	}
}
