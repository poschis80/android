package xpshome.net.util;

import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import net.ktc.mts.logic.Global;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;


//http://www.androidauthority.com/use-android-keystore-store-passwords-sensitive-information-623779/
//http://developer.android.com/intl/ja/training/articles/keystore.html


/**
 * Created by Poschinger Christian on 15.01.2016.
 */
public class Security {

    // TODO : change to an more secure algorithm

    public static final String DEFAULT_ALIAS = "xpshome_crypt";
    private static final String provider = "AndroidKeyStore";
    private static final String defaultAlg = "RSA/ECB/PKCS1Padding"; //"AES/ECB/NoPadding";
    private static KeyStore keyStore = null;
    private static String keyCN = null;
    public Security(String keyCN) {
        try {
            keyStore = KeyStore.getInstance(provider);
            keyStore.load(null);
            this.keyCN = keyCN;
            createNewKeyIfNotExists(DEFAULT_ALIAS);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPrincipalString(String alias) {
        if (keyCN != null) {
            StringBuffer sb = new StringBuffer("CN=");
            sb.append(keyCN).append(" OU=").append(Build.VERSION.SDK_INT).append(" O=").append(alias);
            return sb.toString();
        }
        StringBuffer sb = new StringBuffer("CN=net.xpshome OU=");
        sb.append(Build.VERSION.SDK_INT).append(" O=").append(alias);
        return sb.toString();
    }

    private static AlgorithmParameterSpec getAlgorithmParameterSpec(String alias, int validForYears) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, validForYears);
        // generate specs for the available API Level
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {  // < 18
            return  new RSAKeyGenParameterSpec (1024, RSAKeyGenParameterSpec.F4);

        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) { // <= 22
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(Global.getContext())
                    .setAlias(alias)
                    //.setKeySize(1024)
                    .setSubject(new X500Principal(getPrincipalString(alias)))
                    .setSerialNumber(BigInteger.valueOf(14568))
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            return spec;

        } else {  // >= 23
            // http://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html

            // Important decryption is possible only with private key
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                //.setKeySize(1024)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setCertificateSubject(new X500Principal(getPrincipalString(alias)))
                .setCertificateSerialNumber(BigInteger.valueOf(14568))
                .setCertificateNotBefore(start.getTime())
                .setCertificateNotAfter(end.getTime())
                .build();
            return spec;
        }
    }

    private static String getAlgorithm(String alias) {
        try {
            if (keyStore != null && keyStore.containsAlias(alias)) {
                Certificate c = keyStore.getCertificate(alias);
                String cs = c.toString();
                if (cs.contains("OU=")) {
                    // TODO :
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return defaultAlg;
    }

    public static boolean createNewKey(String alias) {
        if (keyStore != null) {
            try {
                if (!keyStore.containsAlias(alias)) {
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    // Note that the validForYears parameter is available with API level >= 18
                    generator.initialize(getAlgorithmParameterSpec(alias, 999));  // TODO : choose a valid range of years

                    KeyPair keyPair = generator.generateKeyPair();
                    return true;
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean createNewKeyIfNotExists(String alias) {
        try {
            if (keyStore != null)
            {
                if (keyStore.containsAlias(alias)) {
                    return true;
                }
                return createNewKey(alias);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method will replace the old key with an newly generated one.
     * Please note that the old key will be no longer available. Important if you have something encrypted with the old one.
     * @param alias the key alias name to locate inside the storage
     * @return true if succeeded
     */
    public static boolean updateKey(String alias) {
        if (keyStore != null) {
            try {
                if (keyStore.containsAlias(alias)) {
                    keyStore.deleteEntry(alias);
                    return createNewKey(alias);
                }
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static PublicKey getCryptoPublicKey(String alias) {
        try {
            if (keyStore != null && keyStore.containsAlias(alias)) {
                KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
                return privateKeyEntry.getCertificate().getPublicKey();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PrivateKey getCryptPrivateKey(String alias) {
        try {
            if (keyStore != null && keyStore.containsAlias(alias)) {
                try {
                    KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
                    return privateKeyEntry.getPrivateKey();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] operateWithCipher(Cipher cipher, Key key, byte[] input, int cipherMode) throws InvalidKeyException, IOException {
        if (key != null) {
            if (input.length <= 256) {  // TODO : change from RSA block cipher to RSA Key encryption/decryption and use AES for block cipher  https://www.daniweb.com/programming/software-development/threads/231092/java-encryption-rsa-block-size
                return operateWithCipherBlock(cipher, key, input, cipherMode);
            } else {  // encrypt in blocks with size 100
                ArrayList<byte[]> val = divideArray(input, cipherMode == Cipher.ENCRYPT_MODE ? 100 : 256);
                ArrayList<byte[]> res = new ArrayList<>();
                for (byte[] b : val) {
                    res.add(operateWithCipherBlock(cipher, key, b, cipherMode));
                }
                return combineArray(res);
            }
        }
        return new byte[0];
    }

    private static ArrayList<byte[]> divideArray(byte[] input, int chunkSize) {
        ArrayList<byte[]> ret = new ArrayList<>();
        int size = (int)Math.ceil(input.length / (double)chunkSize);
        int start = 0;
        for (int i = 0 ; i < size; i++) {
            ret.add(Arrays.copyOfRange(input, start, ((start + chunkSize) <= input.length) ? start + chunkSize : input.length));
            start += chunkSize;
        }
        return ret;
    }

    private static byte[] combineArray(ArrayList<byte[]> input) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (byte[] b : input) {
            try {
                bos.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bos.toByteArray();
    }

    private static byte[] operateWithCipherBlock(Cipher cipher, Key key, byte[] input, int cipherMode) throws InvalidKeyException, IOException {
        cipher.init(cipherMode, key);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CipherOutputStream cos = new CipherOutputStream(bos, cipher);
        cos.write(input);
        cos.close();
        return bos.toByteArray();
    }

    public static class Util {
        public static <T> byte[] toByteArray(T value) {
            if (value instanceof byte[]) {
                return (byte[])value; // for easier handling we return the original byte array
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream(4);
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                if (value instanceof Boolean) {
                    dos.writeBoolean((Boolean)value);
                } else if (value instanceof Long) {
                    dos.writeLong((Long) value);
                } else if (value instanceof Integer) {
                    dos.writeInt((Integer)value);
                } else if (value instanceof Float) {
                    dos.writeFloat((Float) value);
                } else if (value instanceof String) {
                    dos.writeUTF((String)value);
                } else {
                    return new byte[0];
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new byte[0];
            }
            return bos.toByteArray();
        }

        @SuppressWarnings("unchecked")
        public static <T> T fromByteArray(byte[] value, T defaultValue) {
            if (value == null) {
                return defaultValue;
            }

            if (defaultValue instanceof byte[]) { // for easier handling we return the original byte array
                return (T) value;
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            DataInputStream dis = new DataInputStream(bis);
            try {
                if (defaultValue instanceof Boolean) {
                    Boolean b = dis.readBoolean();
                    return (T) b;
                } else if (defaultValue instanceof Long) {
                    Long l = dis.readLong();
                    return (T) l;
                } else if (defaultValue instanceof Integer) {
                    Integer i = dis.readInt();
                    return (T) i;
                } else if (defaultValue instanceof Float) {
                    Float f = dis.readFloat();
                    return (T) f;
                } else if (defaultValue instanceof String) {
                    String s = dis.readUTF();
                    return (T) s;
                } else {

                    return defaultValue;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return defaultValue;
            }
        }
    }

    public static class NoUserInteraction {
        public static byte[] encrypt(String alias, byte[] input) {
            if (input != null) {
                try {
                    // TODO : check if we have a prblem with API level < 18
                    // TODO : find a way to use RSA/ECB/OAEPWithSHA-512AndMGF1Padding for API level 23+
                    return operateWithCipher(
                            Cipher.getInstance(getAlgorithm(alias)),
                            getCryptoPublicKey(alias),
                            input,
                            Cipher.ENCRYPT_MODE);

                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
            return new byte[0];
        }

        public static byte[] decrypt(String alias, byte[] input) {
            //PrivateKey key = getCryptPrivateKey(alias);
            if (input != null) {
                // TODO : check if we have a prblem with API level < 18
                // TODO : find a way to use RSA/ECB/OAEPWithSHA-512AndMGF1Padding for API level 23+
                try {
                    return operateWithCipher(
                            Cipher.getInstance(getAlgorithm(alias)),
                            getCryptPrivateKey(alias),
                            input,
                            Cipher.DECRYPT_MODE);
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
            return new byte[0];
        }
    }

/*
// TODO : implement when needed
    public static class WithUserInteraction {
        public static byte[] encrypt(String alias, byte[] input) {
            return new byte[0];
        }

        public static byte[] decrypt(String alias, byte[] input) {
            return new byte[0];
        }
    }
*/
}
