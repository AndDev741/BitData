package com.anddev741.BitData;

import org.springframework.boot.SpringApplication;

public class TestBitDataApplication {

	public static void main(String[] args) {
		SpringApplication.from(BitDataApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
