package com.example.vistanotas.models.pagos;

import java.util.List;

public class PagosResponse {
    private List<Pago> pagos;

    public List<Pago> getPagos() {
        return pagos;
    }
    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }
}