package tv.oxnu0xuu.hachimiviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// This DTO matches the 'owner' structure the frontend JS expects
@Getter
@Setter
@AllArgsConstructor
public class OwnerDto {
    private String name;
    private String face; // The frontend expects 'face', our DB has 'avatarUrl'
}