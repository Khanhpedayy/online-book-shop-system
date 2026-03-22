$dir = "d:\online-book-shop-system-main\online_book_shop_system\backend\src\main\java"
$files = Get-ChildItem -Path $dir -Recurse -Filter *.java
$classNames = $files | Select-Object -ExpandProperty BaseName | Sort-Object Length -Descending

Write-Host "Found $($files.Count) Java files."

foreach ($file in $files) {
    if ($file.BaseName -like "Manager*") { continue }
    
    $content = Get-Content -Raw -Path $file.FullName
    $modified = $false
    
    foreach ($cn in $classNames) {
        if ($cn -like "Manager*") { continue }
        if ($content -match "\b$cn\b") {
            $content = [System.Text.RegularExpressions.Regex]::Replace($content, "\b$cn\b", "Manager$cn")
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
    }
}

foreach ($file in $files) {
    if ($file.BaseName -like "Manager*") { continue }
    $newName = "Manager" + $file.Name
    Rename-Item -Path $file.FullName -NewName $newName
}

Write-Host "Done!"
