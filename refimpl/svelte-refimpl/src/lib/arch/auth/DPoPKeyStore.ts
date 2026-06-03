let keyPair: CryptoKeyPair | null = null;

export async function getKeyPair(): Promise<CryptoKeyPair> {
  keyPair ??= await crypto.subtle.generateKey({ name: 'ECDSA', namedCurve: 'P-256' }, true, [
    'sign',
    'verify'
  ]);
  return keyPair;
}

export async function getJwk(): Promise<JsonWebKey> {
  const kp = await getKeyPair();
  return await crypto.subtle.exportKey('jwk', kp.publicKey);
}
