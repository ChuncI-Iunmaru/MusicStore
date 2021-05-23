package tu.kielce.walczak.MusicStore.service;

import tu.kielce.walczak.MusicStore.dto.Purchase;
import tu.kielce.walczak.MusicStore.dto.PurchaseResponse;

public interface CheckoutService {
    PurchaseResponse placeOrder(Purchase purchase);
}
