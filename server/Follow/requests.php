<?php
include "../connect.php";
include "../headers.php";

$response = array();

// Check if 'my_id' is provided in the POST request
if (!isset($_POST['my_id'])) {
    $response['status'] = "failure";
    $response['message'] = "User ID (my_id) not provided";
    echo json_encode($response);
    exit();
}

$my_id = $_POST['my_id'];

// SQL to get the followed users' IDs
$sql = "
    SELECT 
        f.fol_id, 
        u.username, 
        p.picture 
    FROM 
        follow f
    JOIN 
        users u ON f.fol_id = u.id
    JOIN 
        profile p ON f.fol_id = p.id
    WHERE 
        f.person_id = ?
";

$stmt = $conn->prepare($sql);

if ($stmt) {
    $stmt->bind_param("i", $my_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $followed_profiles = array();

        while ($row = $result->fetch_assoc()) {
            $followed_profiles[] = array(
                'id' => $row['fol_id'],
                'username' => $row['username'],
                'profile_pic' => $row['picture']
            );
        }

        $response['status'] = "success";
        $response['followed_profiles'] = $followed_profiles;
    } else {
        $response['status'] = "failure";
        $response['message'] = "No followed users found";
    }

    $stmt->close();
} else {
    $response['status'] = "failure";
    $response['message'] = "Statement preparation failed";
}

$conn->close();

echo json_encode($response);
?>
