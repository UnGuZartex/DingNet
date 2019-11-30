package util;

public class Connection {
    private long from;
    private long to;


    public Connection(long from, long to) {
        this.from = from;
        this.to = to;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public boolean equals(Object o) {
        if (o instanceof Connection) {
            Connection c = (Connection) o;
            return (c.from == this.from) && (c.to == this.to);
        }
        return false;
    }
}
