const stripe = Stripe('pk_test_51QwD35QapQaeEh47GnnDapy5aeveXRt7mmjBEQPjdUjPWYJbLdPItRk2MiLOkDAw8IUqeQVihMqrfh3iq4iOvafk00WlPcPkBv');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
	stripe.redirectToCheckout({
		sessionId: sessionId
	})
});