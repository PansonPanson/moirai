package top.panson.moiraicore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    /**
     * accessToken
     */
    private String accessToken;

    /**
     * tokenTtl
     */
    private Long tokenTtl;
}
