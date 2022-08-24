package com.openet.FW_payload_decoder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class Password {

private String PlainText = null;

public class Vigenere9_3 {

  private static final String defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
/*
 *
 *  map ordinal to character in the alphabet.
*/
  String alphabet;
/*
 * map character in the alphabet to an ordinal. An array of size 2^(sizeof char * 8) == 65536 would probably
 * increase running speed and memory usage.
*/
  Hashtable ordinal = new Hashtable();
/*
 * encryption/decryption key
*/
  String cipherKey;
  String uncipherKey;
  int keylen;
  int alphalen;
/* 
 * encrypt or decrypt a specific string. 'direction' indicates wheather to add or subtract the key from
 * the input, ie, encrypt = 1, or decrypt = -1.
*/
  private String execute(String from, String key) {
    StringBuffer result = new StringBuffer(from.length());
    for (int i = 0; i < from.length(); i++) {
/*
 * get ordinal value of the current character in the cipher/plaintext, and the ordinal value of the matching
 * character from the key.
*/
    	int keyi = ((Integer) ordinal.get(new Character(key.charAt(i % keylen)))).intValue();
    	int fromi = ((Integer) ordinal.get(new Character(from.charAt(i)))).intValue();
/* 
 * output character is the sum of the two, modulo the size of the alphabet. The addition of the extra
 * alphabet.length() ensures that the sum > 0, for the modulus operation to work.
*/
      int toi = (fromi + keyi) % alphalen;
      result.append(alphabet.charAt(toi));
    }
    return result.toString();
  }
/*
 * Public interface
*/
// Create Vigenere cipher for key p_key, and for strings limited to alphabet p_alphabet.
  public Vigenere9_3 (String p_key, String p_alphabet) {
    alphabet = p_alphabet == null ? defaultAlphabet : p_alphabet;
    alphalen = alphabet.length();
    keylen = p_key.length();
// map from each character to its position in the alphabet.
    for (int i = 0; i < alphabet.length(); i++)
      ordinal.put(new Character(alphabet.charAt(i)), new Integer(i));
// calculate complement of crypto key.
      StringBuffer uncipherBuffer = new StringBuffer(p_key.length());
      for (int i = 0; i < keylen; i++)
        uncipherBuffer.append(alphabet.charAt(alphalen - ((Integer) ordinal.get(new Character(p_key.charAt(i)))).intValue()));
      cipherKey = p_key;
      uncipherKey = uncipherBuffer.toString();
  }
// encrypt or decrypt a string in alphabet with key.
  public String cipher(String plaintext) {
    return execute(plaintext, cipherKey);
  }

  public String uncipher(String ciphertext) {
    return execute(ciphertext, uncipherKey);
  }
}

private class Vigenere {
    private String defaultAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_!$%^&*()-+={[]}:;@~#<,>./ ?\"'\\";

    // map ordinal to character in the alphabet.
    String alphabet;

    // map character in the alphabet to an ordinal. An array of size 2^(sizeof char * 8) == 65536 would probably
    // increase running speed and memory usage.
    Hashtable ordinal = new Hashtable();

    // encryption/decryption key
    String cipherKey;

    String uncipherKey;

    int keylen; // stores key.length()

    int alphalen; // stores alphabet.length()

    private String escapeBackslash(String input) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<input.length();i++) {
            if (input.charAt(i)=='\\'){
                sb.append(input.charAt(i));
            }
            sb.append(input.charAt(i));
        }
        return sb.toString();
    }

    private String unescapeBackslash(String input) {
        StringBuilder sb = new StringBuilder();
        boolean removed = false;
        for(int i=0;i<input.length();i++) {
            if (input.charAt(i)=='\\' && !removed){
                sb.append(input.charAt(i));
                removed=true;
                continue;
            }
            if (input.charAt(i)=='\\' && removed){
                removed=false;
                continue;
            }
            removed = false;
            sb.append(input.charAt(i));
        }
        return sb.toString();
    }

    // encrypt or decrypt a specific string. 'direction' indicates wheather to add or subtract the key from
    // the input, ie, encrypt = 1, or decrypt = -1.
    private String execute(String from, String key) {
        boolean removed = false;
        StringBuffer result = new StringBuffer(from.length());
        for (int i = 0; i < from.length(); i++) {
            // get ordinal value of the current character in the cipher/plaintext, and the ordinal value of the matching
            // character from the key.
            int keyi = ((Integer) ordinal.get(new Character(key.charAt(i % keylen)))).intValue();
            int fromi = ((Integer) ordinal.get(new Character(from.charAt(i)))).intValue();
            // output character is the sum of the two, modulo the size of the alphabet. The addition of the extra
            // alphabet.length() ensures that the sum > 0, for the modulus operation to work.
            int toi = (fromi + keyi) % alphalen;
            result.append(alphabet.charAt(toi));
        }
        return result.toString();
    }

    /*
     * Public interface
     */

    // Create Vigenere cipher for key p_key, and for strings limited to alphabet p_alphabet.
    public Vigenere(String p_key, String p_alphabet) {
        alphabet = p_alphabet == null ? defaultAlphabet : p_alphabet;
        alphalen = alphabet.length();
        keylen = p_key.length();

        // map from each character to its position in the alphabet.
        for (int i = 0; i < alphabet.length(); i++)
            ordinal.put(new Character(alphabet.charAt(i)), new Integer(i));

        // calculate complement of crypto key.
        StringBuffer uncipherBuffer = new StringBuffer(p_key.length());
        for (int i = 0; i < keylen; i++)
            uncipherBuffer.append(alphabet.charAt(alphalen - ((Integer) ordinal.get(new Character(p_key.charAt(i)))).intValue()));

        cipherKey = p_key;
        uncipherKey = uncipherBuffer.toString();
    }

    // encrypt or decrypt a string in alphabet with key.
    public String cipher(String plaintext) {
        String cip =  execute(plaintext, cipherKey);
        return escapeBackslash(cip);
    }

    public String uncipher(String ciphertext) {
        String uncip = unescapeBackslash(ciphertext);
        return execute(uncip, uncipherKey);
    }
}


private class Obfuscator {
    private String obfuscationKey = "6qhnqwnn1pbthi3tkexlubhti9wqkoiotbmp";


    public  String obfuscate(String clear, String version) {
        if (version.equals("FW_9.x") || version.equals("FW_6.x")) {
            Vigenere9_3 obfuscationCipher = new Vigenere9_3(obfuscationKey, null);
            return obfuscationCipher.cipher(clear);
        } else {
            Vigenere obfuscationCipher = new Vigenere(obfuscationKey, null);
            return obfuscationCipher.cipher(clear);
        }
    }

    public  String clarify(String obfuscated, String version) {
        if (version.equals("FW_9.x") || version.equals("FW_6.x")) {
            Vigenere9_3 obfuscationCipher = new Vigenere9_3(obfuscationKey, null);
            return obfuscationCipher.uncipher(obfuscated);
        } else  {
            Vigenere obfuscationCipher = new Vigenere(obfuscationKey, null);
            return obfuscationCipher.uncipher(obfuscated);
        }
    }
}
  Password(String version, String mode, String value) {
    Obfuscator  ob = new Obfuscator ();
    if (mode.equals("encode")) {
			PlainText = ob.obfuscate(value, version);
    } else if (mode.equals("decode")) {
			PlainText = ob.clarify(value, version);
    } else {
			PlainText = null;
    }
  }
	public String Get_PlainText(){
		return PlainText;
	}
  public static void main(String[] args) {
    Password p = new Password(args[0], args[1], args[2]);
	}
}
