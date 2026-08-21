## 1. DTO and Model Updates

- [x] 1.1 Add optional `active` field to [UpdateUserRequest.java](file:///Users/dennisvera/github/air_project_backend_team_26/airProject/src/main/java/com/airproject/airproject/dto/UpdateUserRequest.java) to allow admins to reactivate or deactivate users.

## 2. Service and Controller Updates

- [x] 2.1 Update `UserService.getAllUsers()` to retrieve all users (`findAll()`) without filtering out inactive ones.
- [x] 2.2 Update `UserService.getUserById(id)` to retrieve any user (`findById(id)`) regardless of active status.
- [x] 2.3 Update `UserService.updateUser(id, request)` to look up via `findById(id)` and update `active` if `request.getActive()` is provided.
- [x] 2.4 Update `UserService.assignRole(id, request)` and `UserService.deleteUser(id)` to handle finding users by ID cleanly.
- [x] 2.5 Update OpenAPI summary/description in [UserController.java](file:///Users/dennisvera/github/air_project_backend_team_26/airProject/src/main/java/com/airproject/airproject/controller/UserController.java) for `GET /api/users`.

## 3. Testing and Verification

- [x] 3.1 Update and add unit/integration tests in [UserControllerTest.java](file:///Users/dennisvera/github/air_project_backend_team_26/airProject/src/test/java/com/airproject/airproject/controller/UserControllerTest.java) and [UserServiceTest.java](file:///Users/dennisvera/github/air_project_backend_team_26/airProject/src/test/java/com/airproject/airproject/service/UserServiceTest.java) for retrieving all users (active and inactive) and user reactivation.
- [x] 3.2 Verify authentication after user reactivation.
- [x] 3.3 Run Maven test suite to ensure all tests pass.
