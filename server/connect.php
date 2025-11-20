<?php
// connection.php

$host = "localhost";
$username = "root";
$password = ""; // default is empty in XAMPP
$database = "socially";

// Create connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>
