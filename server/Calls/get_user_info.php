<?php
// get_user_info.php

header('Content-Type: application/json');

// Database connection settings
$host = 'localhost';
$db   = 'your_database_name';
$user = 'your_database_user';
$pass = 'your_database_password';

// Connect to the database
$conn = new mysqli($host, $user, $pass, $db);

// Check the connection
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['error' => 'Database connection failed']);
    exit();
}

// Check if user_id is provided
if (!isset($_GET['user_id'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing user_id']);
    exit();
}

$user_id = intval($_GET['user_id']);

// Prepare SQL statement
$sql = "
    SELECT u.name, p.picture 
    FROM users u
    LEFT JOIN profile p ON u.id = p.id
    WHERE u.id = ?
";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    http_response_code(500);
    echo json_encode(['error' => 'Database query failed']);
    exit();
}

$stmt->bind_param('i', $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    echo json_encode([
        'name' => $row['name'] ?? 'Unknown User',
        'profile_picture' => $row['picture'] ?? null
    ]);
} else {
    echo json_encode([
        'name' => 'Unknown User',
        'profile_picture' => null
    ]);
}

$stmt->close();
$conn->close();
?>
