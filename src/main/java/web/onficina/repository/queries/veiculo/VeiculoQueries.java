package web.onficina.repository.queries.veiculo;

import web.onficina.model.modelOnficina.Veiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import web.onficina.filter.VeiculoFilter;

public interface VeiculoQueries {
    Page<Veiculo> pesquisar(VeiculoFilter filtro, Pageable pageable);
}
