package com.amun.id.test;

import com.nhb.common.encrypt.rsa.KeyPairHelper;

public class GenerateRSAKey {
	public static void main(String[] args) throws Exception {
		String privateKeyPath = "resources/private.key";
		String publicKeyPath = "resources/public.key";

		KeyPairHelper helper = new KeyPairHelper();
		helper.generateKey();
		helper.savePrivateKey(privateKeyPath);
		helper.savePublicKey(publicKeyPath);

		System.out.println("done");
	}
}
