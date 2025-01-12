package com.example.lab2;

public class Turismo {

    private String _id; 
    private FromTo from; 
    private FromTo to;   
    private TimeRange timeRange; 
    private int total;          

    public Turismo() {}

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public FromTo getFrom() {
        return from;
    }

    public void setFrom(FromTo from) {
        this.from = from;
    }

    public FromTo getTo() {
        return to;
    }

    public void setTo(FromTo to) {
        this.to = to;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Turismo{" +
                "_id='" + _id + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", timeRange=" + timeRange +
                ", total=" + total +
                '}';
    }

    public static class FromTo {
        private String comunidad;
        private String provincia;

        public FromTo() {}

        public String getComunidad() {
            return comunidad;
        }

        public void setComunidad(String comunidad) {
            this.comunidad = comunidad;
        }

        public String getProvincia() {
            return provincia;
        }

        public void setProvincia(String provincia) {
            this.provincia = provincia;
        }

        @Override
        public String toString() {
            return "FromTo{" +
                    "comunidad='" + comunidad + '\'' +
                    ", provincia='" + provincia + '\'' +
                    '}';
        }
    }

    public static class TimeRange {
        private String fecha_inicio;
        private String fecha_fin;
        private String period;

        public TimeRange() {}

        public String getFecha_inicio() {
            return fecha_inicio;
        }

        public void setFecha_inicio(String fecha_inicio) {
            this.fecha_inicio = fecha_inicio;
        }

        public String getFecha_fin() {
            return fecha_fin;
        }

        public void setFecha_fin(String fecha_fin) {
            this.fecha_fin = fecha_fin;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        @Override
        public String toString() {
            return "TimeRange{" +
                    "fecha_inicio='" + fecha_inicio + '\'' +
                    ", fecha_fin='" + fecha_fin + '\'' +
                    ", period='" + period + '\'' +
                    '}';
        }
    }
}
