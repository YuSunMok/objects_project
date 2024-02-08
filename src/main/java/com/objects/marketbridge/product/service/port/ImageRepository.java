package com.objects.marketbridge.product.service.port;

import com.objects.marketbridge.product.domain.Image;

public interface ImageRepository {

    void save(Image image);

    Image findById(Long id);

    void delete(Image image);

    void deleteById(Long id);
}
