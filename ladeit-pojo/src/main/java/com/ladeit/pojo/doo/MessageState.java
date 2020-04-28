package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageState
 * @Date 2020/3/14 8:12
 */
@Data
@Table(name = "message_state")
@Entity
public class MessageState {
    /**
     * id
     */
    @Id
    private String id;

    /**
     * message_id
     */
    @Column(name = "message_id")
    private String messageId;

    /**
     * user_id
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * read_flag
     */
    @Column(name = "read_flag")
    private Boolean readFlag;
}
