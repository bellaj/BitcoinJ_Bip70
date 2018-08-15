package org.chapter6.BitcoinJClient;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.protocols.payments.PaymentProtocol;
import org.bitcoinj.protocols.payments.PaymentProtocolException;
import org.bitcoinj.protocols.payments.PaymentSession;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
//VERY IMPORTANT.  SOME OF THESE EXIST IN MORE THAN ONE PACKAGE!
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
public class Payment{

private static org.bitcoin.protocols.payments.Protos.PaymentRequest paymentRequest;

final static NetworkParameters params = TestNet3Params.get();

public static void loadcert(){
	try {
	KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
trustStore.load(null);//Make an empty store
//InputStream fis ="/home/user/deletme/last_bitcoin_payment/key_final/cert.pem"; /* insert your file path here */;
BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File("/home/user/deletme/last_bitcoin_payment/key_final/cert.pem")));

CertificateFactory cf = CertificateFactory.getInstance("X.509");

while (bis.available() > 0) {
    Certificate cert = cf.generateCertificate(bis);
    trustStore.setCertificateEntry("fiddler"+bis.available(), cert);
}
	}catch(Exception e) {
		System.out.println("Boooooooooooooooom");
	}
	System.out.println("Boooooooooooooooom222");

}

public static void main(String[] args) throws PaymentProtocolException, BitcoinURIParseException, ExecutionException, InterruptedException, InsufficientMoneyException, IOException {
//	loadcert();
WalletAppKit kit = new WalletAppKit(params, new File("."), "walletappkit");

kit.startAsync();

kit.awaitRunning();

System.out.println("My address : " + kit.wallet().currentReceiveAddress());//https://bip70.com:8883

String url ="bitcoin:mhc5YipxN6GhRRXtgakRBjrNUCbz6ypg66?amount=0.00888888&message=payment%20request&r=http://bip70.com:3000/request?amount=888888";

if(Float.parseFloat(String.valueOf(kit.wallet().getBalance()))==0.0)

System.out.println("Please send some testnet Bitcoins to your address "+kit.wallet().currentReceiveAddress());

else

	 sendPaymentRequest(url, kit);

}

private static void sendPaymentRequest(String location, WalletAppKit k) {

if (location.startsWith("bitcoin")) {

try {

BitcoinURI paymentRequestURI = new BitcoinURI(location);

ListenableFuture<PaymentSession> future = PaymentSession.createFromBitcoinUri(paymentRequestURI);
System.out.println("sending!tfffffffffffffo");

PaymentSession session = future.get();
System.out.println("sending!yyyyyyyyyyyoupi");

if (session.isExpired()) {

System.out.println("request is expired!");

} else {
	System.out.println("sending!");


//send(session, k);

System.exit(1);

}

} catch (PaymentProtocolException e) {

System.err.println("Error creating payment session " + e.getMessage());

System.exit(1);

} catch (BitcoinURIParseException e) {

System.err.println("Invalid bitcoin uri: " + e.getMessage());

System.exit(1);

} catch (InterruptedException e) {

// Ignore.

} catch (ExecutionException e) {

throw new RuntimeException(e);

}

} else {

FileInputStream stream = null;

try {

File paymentRequestFile = new File(location);

stream = new FileInputStream(paymentRequestFile);

} catch (Exception e) {

System.err.println("Failed to open file: " + e.getMessage());

System.exit(1);

}

try {

paymentRequest = org.bitcoin.protocols.payments.Protos.PaymentRequest.newBuilder().mergeFrom(stream).build();

} catch(IOException e) {

System.err.println("Failed to parse payment request from file " + e.getMessage());

System.exit(1);

}

PaymentSession session = null;

try {

session = new PaymentSession(paymentRequest);

} catch (PaymentProtocolException e) {

System.err.println("Error creating payment session " + e.getMessage());

System.exit(1);

}

}

}

private static void send(PaymentSession session,WalletAppKit k) {

try {

System.out.println("Payment Request");

System.out.println("Coin: " + session.getValue().toFriendlyString());

System.out.println("Date: " + session.getDate());

System.out.println("Memo: " + session.getMemo());

PaymentProtocol.PkiVerificationData identity = session.verifyPki();

if (identity != null) {

System.out.println("Pki-Verified Name: " + identity.displayName);

System.out.println("PKI data verified by: " + identity.rootAuthorityName);

}

else{

System.out.println("PKI not Verified");

}

final SendRequest request = session.getSendRequest();

k.wallet().completeTx(request);

String customerMemo = "thanks for your service";

Address refundAddress = new Address(params,"mfcjN5E6vp2NWpMvH7TM2xvTywzRtNvZWR"); ListenableFuture<PaymentProtocol.Ack> future = session.sendPayment(ImmutableList.of(request.tx),refundAddress, customerMemo);

if (future != null) {

PaymentProtocol.Ack ack = future.get();

k.wallet().commitTx(request.tx);

System.out.println("Memo from merchant :"+ack.getMemo());

} else {

Wallet.SendResult sendResult = new Wallet.SendResult();

sendResult.tx = request.tx;

sendResult.broadcast = k.peerGroup().broadcastTransaction(request.tx);

sendResult.broadcastComplete = sendResult.broadcast.future();

}

} catch (Exception e) {

System.err.println("Failed to send payment " + e.getMessage());

System.exit(1);

} } 


}