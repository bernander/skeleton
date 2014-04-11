package com.bernander.mvc1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bernander.mvc1.dao.UserDAO;
import com.bernander.mvc1.entity.User;


@Service
@Transactional
public class UserServiceImpl implements UserService {

        @Autowired
        private UserDAO userDAO;

        public User getUser(String login) {
                return userDAO.getUser(login);
        }

}
