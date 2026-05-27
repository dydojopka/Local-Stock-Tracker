package org.ashroev.service;

import org.ashroev.model.Product;
import org.ashroev.model.ProductHistory;
import org.ashroev.model.ProductInfo;
import org.ashroev.model.WriteOffReason;
import org.ashroev.repository.ProductHistoryRepository;
import org.ashroev.repository.ProductInfoRepository;
import org.ashroev.repository.ProductRepository;
import org.ashroev.repository.ShoppingListItemRepository;
import org.ashroev.repository.ShoppingListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryManagerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductInfoRepository productInfoRepository;

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Mock
    private ProductHistoryRepository productHistoryRepository;

    @InjectMocks
    private InventoryManager inventoryManager;

    @Test
    void writeOffProductCreatesHistoryForUsedReason() {
        ProductInfo productInfo = new ProductInfo();
        Product product = productWithQuantity(productInfo, 2.0);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        inventoryManager.writeOffProduct(1, Double.valueOf(0.5), "USED");

        ArgumentCaptor<ProductHistory> historyCaptor = ArgumentCaptor.forClass(ProductHistory.class);
        verify(productHistoryRepository).save(historyCaptor.capture());
        verify(productRepository).save(product);

        ProductHistory history = historyCaptor.getValue();
        assertThat(history.getProductInfo()).isSameAs(productInfo);
        assertThat(history.getQuantity()).isEqualTo(0.5);
        assertThat(history.getReason()).isEqualTo(WriteOffReason.USED);
        assertThat(history.getActionDate()).isNotNull();
        assertThat(product.getQuantity()).isEqualTo(1.5);
    }

    @Test
    void writeOffProductDoesNotCreateHistoryForDeleteReason() {
        Product product = productWithQuantity(new ProductInfo(), 2.0);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        inventoryManager.writeOffProduct(1, Double.valueOf(1.0), "DELETE");

        verify(productHistoryRepository, never()).save(any());
        verify(productRepository).save(product);
        assertThat(product.getQuantity()).isEqualTo(1.0);
    }

    private Product productWithQuantity(ProductInfo productInfo, double quantity) {
        Product product = new Product();
        product.setProductInfo(productInfo);
        product.setQuantity(quantity);
        return product;
    }
}
