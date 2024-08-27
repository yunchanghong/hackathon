package com.rakuten.hackathon.data.feed.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.rakuten.hackathon.data.feed.dto.User;

@Repository
public class UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery("SELECT t FROM User t", User.class);
        return query.getResultList();
    }
    
    public List<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery("SELECT t FROM Tutorial t WHERE t.useremail=:email",
                User.class);
        return query.setParameter("email", email).getResultList();
    }
}
