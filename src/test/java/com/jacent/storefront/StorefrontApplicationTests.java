package com.jacent.storefront;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;

@SpringBootTest
class StorefrontApplicationTests {

	@MockitoBean
	DataSource dataSource;

	@Test
	void contextLoads() {
	}

}
