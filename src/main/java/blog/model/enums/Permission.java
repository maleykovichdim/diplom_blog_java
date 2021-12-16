package blog.model.enums;

public enum Permission {

    USER("user:write"),
    MODERATE("user:moderate");

    private final String permissionData;

    Permission (String permission){
        this.permissionData = permission;
    }

    public String getPermission(){
        return permissionData;
    }
}
