package com.bernander.mvc1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bernander.mvc1.dao.RoleDAO;
import com.bernander.mvc1.entity.Role;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

        @Autowired
        private RoleDAO roleDAO;

        public Role getRole(int id) {
                return roleDAO.getRole(id);
        }

}