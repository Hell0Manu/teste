package web.onficina.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.onficina.model.modelOnficina.Veiculo;
import web.onficina.repository.VeiculoRepository;

@Service
@Transactional
public class VeiculoService {
    

    private VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public void salvar(Veiculo veiculo) {
        veiculoRepository.save(veiculo);
    }

    public void alterar(Veiculo veiculo) {
        veiculoRepository.save(veiculo);
    }

    public void remover(Long codigo) {
        veiculoRepository.deleteById(codigo);
    }


}
