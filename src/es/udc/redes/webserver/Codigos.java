package es.udc.redes.webserver;

    /*
        Cada codigo del enumerado se corresponde con un posible fallo del servidor
     */
    public enum Codigos {
        OK("200 OK"),

        NOT_MODIFIED("304 Not Modified"),

        BAD_REQUEST("400 Bad Request"),

        NOT_FOUND("404 Not Found"),

        NOT_IMPLEMENTED("501 Not implemented");

        private final String estado;

        private Codigos(String code){
            this.estado = code;
        }


        public String getEstado() {
            return estado;
        }
    }
