package com.umg.edu.api;

import com.umg.edu.modelo.Cliente;
import com.umg.edu.modelo.Recompensa;
import com.umg.edu.servicio.ClienteServicio;
import com.umg.edu.servicio.FidelizacionServicio;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/clientes")
public class ClienteApiControlador {

    private final ClienteServicio clienteServicio = new ClienteServicio();
    private final FidelizacionServicio fidelizacionServicio = new FidelizacionServicio();

    public record RecompensaDTO(String nombre, int puntosRequeridos, boolean alcanzable) {
    }

    public record SaldoDTO(String identificador, String nombre, int puntos, String nivel, boolean activo,
            List<RecompensaDTO> recompensas) {
    }

    @GetMapping("/{identificador}/puntos")
    public ResponseEntity<?> consultarPuntos(@PathVariable String identificador) {
        Cliente cliente = clienteServicio.buscarPorIdentificador(identificador);
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "No existe un cliente con el identificador " + identificador));
        }
        List<RecompensaDTO> recompensas = fidelizacionServicio.listarRecompensas().stream()
                .filter(Recompensa::isActivo)
                .map(r -> new RecompensaDTO(r.getNombre(), r.getPuntosRequeridos(), r.puedeCanjear(cliente)))
                .toList();
        return ResponseEntity.ok(new SaldoDTO(cliente.getIdentificador(), cliente.getNombreCompleto(),
                cliente.getPuntos(), cliente.getNivel(), cliente.isActivo(), recompensas));
    }
}
