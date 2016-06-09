package com.amun.id;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import com.amun.id.statics.F;
import com.mario.entity.impl.BaseMessageHandler;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.rsa.KeyPairHelper;
import com.nhb.common.encrypt.rsa.SignatureHelper;
import com.nhb.common.utils.Converter;
import com.nhb.common.utils.FileSystemUtils;

public class SignaturePlugin extends BaseMessageHandler {
	private SignatureHelper signatureHelper;

	@Override
	public void init(PuObjectRO initParams) {
		String privateKeyPath = initParams.getString(F.PRIVATE_KEY);
		String publicKeyPath = initParams.getString(F.PUBLIC_KEY);
		KeyPairHelper keyPairHelper = new KeyPairHelper();
		try {
			keyPairHelper.loadPrivateKey(new File(
					FileSystemUtils.createPathFrom(FileSystemUtils.getBasePathForClass(getClass()), privateKeyPath)));
			keyPairHelper.loadPublicKey(new File(
					FileSystemUtils.createPathFrom(FileSystemUtils.getBasePathForClass(getClass()), publicKeyPath)));
		} catch (IOException | GeneralSecurityException e1) {
			throw new RuntimeException("load private key error", e1);
		}

		signatureHelper = new SignatureHelper();
		signatureHelper.setKeyPairHelper(keyPairHelper);
	}

	@Override
	public PuElement interop(PuElement requestParams) {
		if (requestParams instanceof PuObject) {
			PuObject request = (PuObject) requestParams;
			if (request.variableExists(F.COMMAND)) {
				String command = request.getString(F.COMMAND);
				PuObject result = new PuObject();
				switch (command) {
				case "signData": {
					String data = request.getString(F.DATA);
					try {
						byte[] sign = signatureHelper.sign(data);
						String signature = Converter.bytesToHex(sign);
						result.setInteger(F.STATUS, 0);
						result.setString(F.SIGNATURE, signature);
					} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
						getLogger().error("sign data error", e);
						result.setInteger(F.STATUS, 1);
					}
					break;
				}
				case "verify": {
					String info = request.getString(F.INFO);
					String signature = request.getString(F.SIGNATURE);
					try {
						boolean verify = signatureHelper.verify(info.getBytes(), Converter.hexToBytes(signature));
						result.setInteger(F.STATUS, 0);
						result.setBoolean(F.IS_VALID, verify);
					} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
						getLogger().error("verify signature error", e);
						result.setInteger(F.STATUS, 1);
					}
				}
				default:
					break;
				}
				return result;
			}
		}
		return null;
	}
}
