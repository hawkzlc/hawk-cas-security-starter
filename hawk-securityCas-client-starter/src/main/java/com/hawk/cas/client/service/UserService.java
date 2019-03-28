package com.hawk.cas.client.service;

import com.hawk.cas.client.entity.User;
/**
 * 
 * @author Zhoulc & Lucio Zhou
 * @date 2018年12月28日 上午11:23:10
 * @email 34405161@qq.com, 13811858856@163.com
 */
public interface UserService {

	 User findByUsername(String username);
}
