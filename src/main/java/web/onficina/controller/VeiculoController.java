package web.onficina.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import web.onficina.model.Veiculo;
import web.onficina.service.VeiculoService;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    @Autowired
    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public String listarVeiculos(Model model) {
        model.addAttribute("veiculos", veiculoService.buscarTodos());
        return "veiculo/listar";
    }

    @GetMapping("/novo")
    public String novoVeiculoForm(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        return "veiculo/form";
    }

    @PostMapping("/salvar")
    public String salvarVeiculo(@Valid @ModelAttribute("veiculo") Veiculo veiculo, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "veiculo/form";
        }
        veiculoService.salvar(veiculo);
        attributes.addFlashAttribute("mensagem", "Veículo salvo com sucesso!");
        return "redirect:/veiculos";
    }

    @GetMapping("/editar/{id}")
    public String editarVeiculoForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        Optional<Veiculo> veiculoOptional = veiculoService.buscarPorId(id);
        if (veiculoOptional.isPresent()) {
            model.addAttribute("veiculo", veiculoOptional.get());
            return "veiculo/form";
        } else {
            attributes.addFlashAttribute("mensagemErro", "Veículo não encontrado!");
            return "redirect:/veiculos";
        }
    }

    @GetMapping("/remover/{id}")
    public String removerVeiculo(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            veiculoService.excluir(id);
            attributes.addFlashAttribute("mensagem", "Veículo removido com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro ao remover veículo: " + e.getMessage());
        }
        return "redirect:/veiculos";
    }
}
