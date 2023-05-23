
package com.ordg.blueb.pos.returns;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Administrator
 */
@Entity
@Table(name = "EMPLOYEED", catalog = "", schema = "ORDG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EmployeedEo.findAll", query = "SELECT e FROM EmployeedEo e"),
    @NamedQuery(name = "EmployeedEo.findByEmployeeId", query = "SELECT e FROM EmployeedEo e WHERE e.employeeId = :employeeId"),
    @NamedQuery(name = "EmployeedEo.findByEmployeeName", query = "SELECT e FROM EmployeedEo e WHERE e.employeeName = :employeeName"),
    @NamedQuery(name = "EmployeedEo.findByHiringDate", query = "SELECT e FROM EmployeedEo e WHERE e.hiringDate = :hiringDate"),
    @NamedQuery(name = "EmployeedEo.findBySalary", query = "SELECT e FROM EmployeedEo e WHERE e.salary = :salary"),
    @NamedQuery(name = "EmployeedEo.findByStatusFlag", query = "SELECT e FROM EmployeedEo e WHERE e.statusFlag = :statusFlag")
})

public class EmployeedEo implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @Column(name = "EMPLOYEE_ID")
    private BigDecimal employeeId;
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;
    @Column(name = "HIRING_DATE")
    @Temporal(TemporalType.DATE)
    private Date hiringDate;
    @Column(name = "SALARY")
    private BigDecimal salary;
    @Column(name = "STATUS_FLAG")
    private Character statusFlag;

    public EmployeedEo() {
    }

    public EmployeedEo(BigDecimal employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(BigDecimal employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getHiringDate() {
        return hiringDate;
    }

    public void setHiringDate(Date hiringDate) {
        this.hiringDate = hiringDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Character getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(Character statusFlag) {
        this.statusFlag = statusFlag;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (employeeId != null ? employeeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EmployeedEo)) {
            return false;
        }
        EmployeedEo other = (EmployeedEo) object;
        if ((this.employeeId == null && other.employeeId != null) || (this.employeeId != null && !this.employeeId.equals(other.employeeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ordg.pos.bluebpos.databse.EO.Employeed[ employeeId=" + employeeId + " ]";
    }
    
}
