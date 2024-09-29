package com.jftse.b2b.rest;

import com.jftse.b2b.dto.ProductDto;
import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.repository.item.ProductRepository;
import com.jftse.server.core.item.EItemCategory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("shop")
public class ShopController {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll(@PageableDefault(size = 50, sort = "productIndex") Pageable pageable) {
        List<Product> productList = productRepository.findAll(pageable).getContent();
        List<ProductDto> productDtoList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(productDtoList);
    }

    @GetMapping("/getByProductIndex")
    public ResponseEntity<ProductDto> getByProductIndex(@RequestParam Integer productIndex) throws ValidationException {
        if (productIndex < 0)
            throw new ValidationException("productIndex must be greater than 0");

        Product product = productRepository.findProductByProductIndex(productIndex);
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        return ResponseEntity.ok()
                .body(productDto);
    }

    @GetMapping("/getAllByCategoryAndEnabled")
    public ResponseEntity<List<ProductDto>> getAllByCategoryAndEnabled(@RequestParam String category, @RequestParam(defaultValue = "true") boolean enabled, @PageableDefault(sort = "productIndex") Pageable pageable) throws ValidationException {
        validateCategory(category);

        List<Product> productList = productRepository.findAllByCategoryAndEnabled(category.toUpperCase(), enabled, pageable);
        List<ProductDto> productDtoList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(productDtoList);
    }

    @GetMapping("/getAllByCategoryAndEnabledAndPriceType")
    public ResponseEntity<List<ProductDto>> getAllByCategoryAndEnabledAndPriceType(@RequestParam String category, @RequestParam(defaultValue = "true") boolean enabled, @RequestParam String priceType, @PageableDefault(sort = "productIndex") Pageable pageable) throws ValidationException {
        validateCategory(category);
        validatePriceType(priceType);

        List<Product> productList = productRepository.findAllByCategoryAndEnabledAndPriceType(category.toUpperCase(), enabled, priceType.toUpperCase(), pageable);
        List<ProductDto> productDtoList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(productDtoList);
    }

    @GetMapping("/getByItem0AndCategory")
    public ResponseEntity<ProductDto> getAllByItem0AndCategory(@RequestParam Integer itemIndex, @RequestParam String category) throws ValidationException {
        validateCategory(category);

        if (itemIndex < 0)
            throw new ValidationException("itemIndex must be greater than 0");

        List<Product> product = productRepository.findProductsByItem0AndCategory(itemIndex, category.toUpperCase());
        if (product.isEmpty())
            throw new ValidationException("product not found");

        ProductDto productDto = modelMapper.map(product.get(0), ProductDto.class);

        return ResponseEntity.ok()
                .body(productDto);
    }

    @GetMapping("/getByNameAndCategory")
    public ResponseEntity<ProductDto> getProductByNameAndCategory(@RequestParam String name, @RequestParam String category) throws ValidationException {
        validateCategory(category);

        List<Product> product = productRepository.findProductsByNameAndCategory(name, category.toUpperCase());
        if (product.isEmpty())
            throw new ValidationException("product not found");

        ProductDto productDto = modelMapper.map(product.get(0), ProductDto.class);

        return ResponseEntity.ok()
                .body(productDto);
    }

    @GetMapping("/getAllByPrice0Between")
    public ResponseEntity<List<ProductDto>> getAllByPrice0Between(@RequestParam Integer minPrice, @RequestParam Integer maxPrice, @PageableDefault(sort = "productIndex") Pageable pageable) throws ValidationException {
        if (minPrice < 0)
            throw new ValidationException("minPrice must be greater than 0");
        if (maxPrice < 0)
            throw new ValidationException("maxPrice must be greater than 0");

        List<Product> productList = productRepository.findAllByPrice0Between(minPrice, maxPrice, pageable);
        List<ProductDto> productDtoList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(productDtoList);
    }

    private void validateCategory(String category) throws ValidationException {
        Stream<EItemCategory> stream = Stream.of(EItemCategory.values());
        if (stream.noneMatch(eItemCategory -> eItemCategory.getName().equalsIgnoreCase(category)))
            throw new ValidationException("category " + category + " is not valid");
    }

    private void validatePriceType(String priceType) throws ValidationException {
        Stream<PriceType> stream = Stream.of(PriceType.values());
        if (stream.noneMatch(pt -> pt.getName().equalsIgnoreCase(priceType)))
            throw new ValidationException("priceType " + priceType + " is not valid");
    }
}
