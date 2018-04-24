package mod

import main.Game
import java.security.*

class ModPermissionsPolicy : Policy() {

    override fun getPermissions(codesource: CodeSource?): PermissionCollection {
        val p = Permissions()
        if(codesource != null) {
            if (codesource.location.path == Game.JAR_PATH) {
                p.add(AllPermission())
            }
        }
        p.add(AllPermission())
        return p
    }

    override fun refresh() {
    }
}