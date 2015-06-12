package org.javagems.core.hibernate;

import java.util.Date;

public interface Auditable {

    public void setModified(Date date);

    public Date getModified();

    public void setCreated(Date date);

    public Date getCreated();
}
