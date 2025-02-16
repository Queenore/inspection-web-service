package com.service.inspection.entities;

import com.service.inspection.entities.enums.FileTypes;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "file_inspection")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileInspection extends FileEntity {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private FileTypes type;

    @Column(name = "creation_date")
    private OffsetDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "inspection_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    private Inspection inspection;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        FileInspection that = (FileInspection) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
