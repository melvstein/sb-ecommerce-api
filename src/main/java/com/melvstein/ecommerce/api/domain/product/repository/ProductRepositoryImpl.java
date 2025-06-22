package com.melvstein.ecommerce.api.domain.product.repository;

import com.melvstein.ecommerce.api.domain.product.document.Product;
import com.melvstein.ecommerce.api.domain.product.dto.FilterPartsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Product> filter(List<String> filter, Pageable pageable) {
        Query query = new Query();
        List<Criteria> andCriteriaList = new ArrayList<>();
        List<Criteria> orCriteriaList = new ArrayList<>();

        // Always include active products only
        andCriteriaList.add(Criteria.where("isActive").is(true));

        if (filter != null && !filter.isEmpty()) {
            for (String param : filter) {
                String[] parts = param.split(":");

                if (parts.length == 2) {
                    String field = parts[0].trim();
                    String value = parts[1].trim();
                    andCriteriaList.add(Criteria.where(field).is(value));
                } else if (parts.length == 3) {
                    String field = parts[0].trim();
                    String logic = parts[1].trim();
                    String value = parts[2].trim();

                    FilterPartsDto dto = FilterPartsDto.builder()
                            .field(field)
                            .logic(logic)
                            .value(value)
                            .build();

                    andCriteriaList.add(buildCriteriaLogic(dto));
                } else if (parts.length == 4) {
                    String operator = parts[0].trim();
                    String field = parts[1].trim();
                    String logic = parts[2].trim();
                    String value = parts[3].trim();

                    FilterPartsDto dto = FilterPartsDto.builder()
                            .operator(operator)
                            .field(field)
                            .logic(logic)
                            .value(value)
                            .build();

                    Criteria logicCriteria = buildCriteriaLogic(dto);

                    if ("or".equalsIgnoreCase(operator)) {
                        orCriteriaList.add(logicCriteria);
                    } else if ("and".equalsIgnoreCase(operator)) {
                        andCriteriaList.add(logicCriteria);
                    } else {
                        log.warn("Unknown operator: {}", operator);
                    }
                } else {
                    log.warn("Invalid filter format: {}", param);
                }
            }
        }

        Criteria finalCriteria;
        if (!orCriteriaList.isEmpty()) {
            finalCriteria = new Criteria().andOperator(
                    andCriteriaList.toArray(new Criteria[0])
            ).orOperator(orCriteriaList.toArray(new Criteria[0]));
        } else {
            finalCriteria = new Criteria().andOperator(andCriteriaList.toArray(new Criteria[0]));
        }

        query.addCriteria(finalCriteria);

        log.info("Generated MongoDB Query: {}", query);
        query.with(pageable);

        return mongoTemplate.find(query, Product.class);
    }

    public Criteria buildCriteriaLogic(FilterPartsDto parts) {
        return switch (parts.logic()) {
            case "eq" -> Criteria.where(parts.field()).is(parts.value());
            case "ne" -> Criteria.where(parts.field()).ne(parts.value());
            case "gt" -> Criteria.where(parts.field()).gt(parts.value());
            case "gte" -> Criteria.where(parts.field()).gte(parts.value());
            case "lt" -> Criteria.where(parts.field()).lt(parts.value());
            case "lte" -> Criteria.where(parts.field()).lte(parts.value());
            case "in" -> Criteria.where(parts.field()).in(List.of(parts.value().split("_")));
            default -> throw new IllegalArgumentException("Invalid logic: " + parts.logic());
        };
    }
}